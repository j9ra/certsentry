package pl.grabojan.certsentry.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateServiceHelper {
	
	private final CertificateFactory cf;
	private final Map<String,String> principalNamesMap;
	
	public CertificateServiceHelper() {
		try {
			cf = CertificateFactory.getInstance("X509","BC");
		} catch(CertificateException e) {
			throw new CertificateServiceFailureException("Failed to initialize X509 CertificateFactory",e);
		} catch (NoSuchProviderException e) {
			throw new CertificateServiceFailureException("Failed to initialize X509 CertificateFactory",e);
		}
		principalNamesMap = initOidMap();
	}
	
	public X509Certificate parseCertificate(byte[] certBin) {
		try {
			return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBin));
		} catch(CertificateException e) {
			log.error("Invalid certificate [{}]", wrapBinToString(certBin));
			throw new CertificateServiceFailureException("Cert parse failed ",e);
		} 	
	}
	
	public X509Certificate parseCertificate(String cert) {
		byte[] certBlob = null;
		try {
			certBlob = Base64.getDecoder().decode(cert.getBytes());
		} catch(Exception e) {
			log.error("Invalid certificate [{}]", cert);
			throw new CertificateServiceFailureException("Cert base64 decode failed ",e);
		}
		return parseCertificate(certBlob);
	}
	
	public List<X509Certificate> parseCertificatePath(byte[] certPathBin) {
		
		try {
			CertPath cp = cf.generateCertPath(new ByteArrayInputStream(certPathBin), "PKCS7");
			return cp.getCertificates().stream().
					map(c -> (X509Certificate)c).
					collect(Collectors.toList());
		} catch (CertificateException e) {
			log.error("Invalid certpath [{}]", wrapBinToString(certPathBin));
			throw new CertificateServiceFailureException("CertPath parse failed ",e);
		}
		
	}
	
	public String certificateToString(X509Certificate cert) {
		try {
			byte[] certBlob = cert.getEncoded();
			return Base64.getEncoder().encodeToString(certBlob);
		} catch (CertificateException e) {
			log.error("Unable to encode certificate " + cert, e);
			throw new CertificateServiceFailureException("Certificate encode failed ",e);
		}
	}
	
	public X509CRL parseCRL(byte[] crlBin) {
		try {
			return (X509CRL)cf.generateCRL(new ByteArrayInputStream(crlBin));
		} catch (CRLException e) {
			log.error("Invalid CRL [{}]", wrapBinToString(crlBin));
			throw new CertificateServiceFailureException("CRL parse failed ",e);
		}
	}
	
	public String resolveX500Principal(X500Principal principal) {
		return principal.getName(X500Principal.RFC1779,principalNamesMap);
	}
	
	public String getPublicKeyHash(PublicKey pk) {
		return new String(Hex.encodeHex(DigestUtils.sha256(pk.getEncoded())));
	}
	
	public String getOcspResponderURI(X509Certificate cert) {
		
		X509CertificateHolder certHolder = null;
		try {
			certHolder = new X509CertificateHolder(cert.getEncoded());
		} catch (CertificateEncodingException|IOException e) {
			throw new RuntimeException("Unable to create X509CertificateHolder instance",e);
		} 
		
		Extension aiaExt = certHolder.getExtensions().getExtension(Extension.authorityInfoAccess);
		if(aiaExt == null) {
			return null;
		}
					
		AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(aiaExt.getParsedValue());
		
		AccessDescription[] accessDescs = aia.getAccessDescriptions();
		for (AccessDescription ad : accessDescs) {
			if(ad.getAccessMethod().equals(AccessDescription.id_ad_ocsp)) {
				GeneralName gn = ad.getAccessLocation();
				if(gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
					return ad.getAccessLocation().getName().toString();
				}
			}
		}
		
		return null;
		
	}
	
	public String getCaIssuersURI(X509Certificate cert) {
		
		X509CertificateHolder certHolder = null;
		try {
			certHolder = new X509CertificateHolder(cert.getEncoded());
		} catch (CertificateEncodingException|IOException e) {
			throw new RuntimeException("Unable to create X509CertificateHolder instance",e);
		} 
		
		Extension aiaExt = certHolder.getExtensions().getExtension(Extension.authorityInfoAccess);
		if(aiaExt == null) {
			return null;
		}
					
		AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(aiaExt.getParsedValue());
		
		AccessDescription[] accessDescs = aia.getAccessDescriptions();
		for (AccessDescription ad : accessDescs) {
			if(ad.getAccessMethod().equals(AccessDescription.id_ad_caIssuers)) {
				GeneralName gn = ad.getAccessLocation();
				if(gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
					return ad.getAccessLocation().getName().toString();
				}
			}
		}
		
		return null;
		
	}
	
	public String getCRLDPServerURI(X509Certificate cert) {
		X509CertificateHolder certHolder = null;
		try {
			certHolder = new X509CertificateHolder(cert.getEncoded());
		} catch (CertificateEncodingException|IOException e) {
			throw new RuntimeException("Unable to create X509CertificateHolder instance",e);
		} 
		
		Extension crlDpExt = certHolder.getExtensions().getExtension(Extension.cRLDistributionPoints);
		if(crlDpExt == null) {
			return null;
		}
		
		CRLDistPoint crlDistPoints = CRLDistPoint.getInstance(crlDpExt.getParsedValue());
		
		DistributionPoint[] dpPoints = crlDistPoints.getDistributionPoints();
		for (DistributionPoint dp : dpPoints) {
			DistributionPointName dpName = dp.getDistributionPoint();
			if(dpName != null) {
				if(dpName.getType() == DistributionPointName.FULL_NAME) {
					ASN1Encodable encodalble = dpName.getName();
					GeneralNames gn = GeneralNames.getInstance(encodalble);
					GeneralName[] names = gn.getNames();
					for (GeneralName name : names) {
						if(name.getTagNo() == GeneralName.uniformResourceIdentifier) {
							String ns = name.getName().toString();
							if(ns.startsWith("http")) {
								return ns;
							}
						}
					}
				}
				
				
				
			}
		}
		return null;
	}
	
	
	private Map<String,String> initOidMap() {
		Map<String,String> oidMap = new HashMap<String, String>();
		oidMap.put("2.5.4.5", "SERIALNUMBER");
		oidMap.put("2.5.4.97", "ORGANIZATIONIDENTIFIER");
		oidMap.put("1.2.840.113549.1.9.1", "EMAILADDRESS");
		oidMap.put("0.2.262.1.10.7.20", "NAMEDISTINGUISHER");
		oidMap.put("0.9.2342.19200300.100.1.25", "DC");
		oidMap.put("2.5.4.20", "TELEPHONENUMBER");
		return oidMap;
	}
	
	private String wrapBinToString(byte[] bin) {
		try {
			return Base64.getEncoder().encodeToString(bin);
		} catch(Exception e) {
			return "(unable to stringify)";
		}
	}
	
}
