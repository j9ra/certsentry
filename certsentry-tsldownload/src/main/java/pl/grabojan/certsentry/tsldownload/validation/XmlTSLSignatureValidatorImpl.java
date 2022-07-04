package pl.grabojan.certsentry.tsldownload.validation;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.xml.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.tsldownload.util.PooledDocumentBuilder;

@Slf4j
@RequiredArgsConstructor
public class XmlTSLSignatureValidatorImpl implements XmlTSLSignatureValidator {

	private final XPathExpression certDigestMethod;
	private final XPathExpression certDigestValue;
	private final XPathExpression certIssuerName;
	private final XPathExpression certSerialNumber;
	private final PooledDocumentBuilder pooledDocumentBuilder;
	
	@Override
	public boolean validate(InputStream inputStream, TSLValidatorContext ctx) {
				
		Document doc = null;
		try {
			doc = pooledDocumentBuilder.createDocumentBuilder().parse(inputStream);
		} catch (SAXException | IOException e) {
			log.error("Xml document parse failed", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-1);
			return false;
		}
				
		// Find Signature element.
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		if (nl.getLength() == 0) {
			String msg = "Cannot find Signature element";
			log.error(msg);
			ctx.setErrorMessage(msg);
			ctx.setErrorCode(-2);
			return false;
		}

		// Create a DOMValidateContext and specify a KeySelector
		// and document context.
		DOMValidateContext valContext = new DOMValidateContext(new KeySelector() {

			@Override
			public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method,
					XMLCryptoContext context) throws KeySelectorException {

				X509Certificate cert = locateX509Certificate(keyInfo);
				if (cert != null) {
					final PublicKey key = cert.getPublicKey();
					return new KeySelectorResult() {
						public Key getKey() {
							return key;
						}
					};
				} else {
					throw new KeySelectorException("No key found!");
				}

			}

		}, nl.item(0));
		
	
		XMLSignatureFactory fac = null;
		try {
				fac = XMLSignatureFactory.getInstance("DOM","ApacheXMLDSig");
		} catch(NoSuchProviderException e) {
			log.error("Provider ApacheXMLDsig is not registred");
			throw new RuntimeException("Unable to create XMLSignatureFactory", e);
		}
				
		// Unmarshal the XMLSignature.
		XMLSignature signature = null;
		try {
			signature = fac.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			log.error("XmlSignature element unmarshal failed", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-3);
			return false;
		}
				
		// Validate the XMLSignature.
		try {
			boolean coreValidity = signature.validate(valContext);
					
			if (coreValidity == false) {
			    log.error("Signature failed validation - errors follows");
			    boolean sv = signature.getSignatureValue().validate(valContext);
				log.error("signature validation status: " + sv);
					    
				// Check the validation status of each Reference.
				for (Object refObj : signature.getSignedInfo().getReferences()) {
					Reference ref = (Reference)refObj;			
					boolean refValid = ref.validate(valContext);
					log.error("Reference uri {} validity status {}", ref.getURI(), refValid);
				}
				ctx.setErrorMessage("XmlSignature validation failed");
				ctx.setErrorCode(-4);
				return false;
			}
					
		} catch (XMLSignatureException e) {
			log.error("XmlSignature error during validation", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-5);
			return false;
		}
				
		
		// Get signing key for validation
		KeySelectorResult keySelectorResult = signature.getKeySelectorResult();
		PublicKey signKey = (PublicKey)keySelectorResult.getKey(); 
		// locate certificate
		X509Certificate signCert = locateX509Certificate(signature.getKeyInfo());
		if(signCert == null || !signCert.getPublicKey().equals(signKey)) {
			// Keyinfo certificate dont match sign key! 
			String msg = "KeyInfo X509Data certificate dont match validation PublicKey";
			log.error(msg);
			ctx.setErrorMessage(msg);
			ctx.setErrorCode(-6);
			return false;
		}
				
				
		// locate xades referenced signed properties
		String xadesSigPropId = null;
		for (Object o : signature.getSignedInfo().getReferences()) {
			Reference ref = (Reference)o;
			if("http://uri.etsi.org/01903#SignedProperties".equals(ref.getType())) {
				String uri = ref.getURI();
				if(uri.startsWith("#")) { // convert xpointer(#ID) to ID
					xadesSigPropId = uri.substring(1);
				}
			}
		}
		if(xadesSigPropId != null) {
				
			Element signPropertiesElem = doc.getElementById(xadesSigPropId);
			if(signPropertiesElem != null) {
				
				String digestMethod = certDigestMethod.evaluateAsString(signPropertiesElem);
				String digestValue = certDigestValue.evaluateAsString(signPropertiesElem);
				String issuerName = certIssuerName.evaluateAsString(signPropertiesElem);
				String serialNumber = certSerialNumber.evaluateAsString(signPropertiesElem);
					
				log.debug("XAdES SignatureProperties cert info; Digest - method:{}, value:{}; Cert - issuer:{}, serial:{} ",
						digestMethod, digestValue, issuerName, serialNumber);
					
				String signCertDigestValue = getCertDigestValueB64(signCert, digestMethod);
					
				X500Principal issuerPrincipal = new X500Principal(issuerName);
					
				if(signCert.getIssuerX500Principal().equals(issuerPrincipal) && 
						signCert.getSerialNumber().toString().equals(serialNumber) &&
						digestValue.equals(signCertDigestValue) && 
						ctx.getSignatureAllowedCertificates().contains(signCert)) {
					return true;
				}
			}
	
		} else {
			// no signedpropeties, check reference for KeyInfo exist if not validation should fail
			String keyInfoId = signature.getKeyInfo().getId();
			boolean keyInfoIsReferenced = false;
			if(keyInfoId != null) {
				for (Object refo : signature.getSignedInfo().getReferences()) {
						Reference r = (Reference)refo;
						String refUri = r.getURI();
						if(refUri != null && refUri.length() > 0 && refUri.substring(1).equals(keyInfoId)) {
							log.debug("XAdES KeyInfo is referenced, ID:{}", keyInfoId);
							keyInfoIsReferenced = true;
						}
				}
			}
			if(keyInfoIsReferenced && ctx.getSignatureAllowedCertificates().contains(signCert)) {
				return true;
			}

		}
		String msg = "Unable to verify signing certificate";
		log.error(msg);
		ctx.setErrorMessage(msg);
		ctx.setErrorCode(-7);
		return false;
				
	}
	
	private String getCertDigestValueB64(X509Certificate cert, String digestMethod) {
		byte[] certBin=null;
		try {
			certBin = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			log.error("Cerificate encoding failed", e);
			return ""; 
		}
		byte[] out = null;
		if(digestMethod.endsWith("sha1")) {
			out = DigestUtils.sha1(certBin);
		} else if(digestMethod.endsWith("sha256")) {
			out = DigestUtils.sha256(certBin);
		} else if(digestMethod.endsWith("sha512")) {
			out = DigestUtils.sha512(certBin);
		} else {
			throw new RuntimeException("Unsupported digestMethod " + digestMethod);
		}

		return Base64.encodeBase64String(out);
	}
	
	@SuppressWarnings("rawtypes")
	private X509Certificate locateX509Certificate(KeyInfo keyInfo) {

		Iterator ki = keyInfo.getContent().iterator();
        while (ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if (!(info instanceof X509Data))
                continue;
            X509Data x509Data = (X509Data) info;
            Iterator xi = x509Data.getContent().iterator();
            while (xi.hasNext()) {
                Object o = xi.next();
                if (!(o instanceof X509Certificate)) {
                    continue;
                }
                
                X509Certificate cert = (X509Certificate)o;
                if(cert.getBasicConstraints() == -1) { 
                	// is not ca cert, so its must be end entity/signer cert
                	return cert;
                }
            }
        }
        return null;
	}
	
}
