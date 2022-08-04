package pl.grabojan.certsentry.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class OcspClientService {
	
	
	public static OCSPReq parseOcspRequest(byte[] blob) {
		
		try {
			return new OCSPReq(blob);
		} catch (IOException e) {
			throw new RuntimeException("Unable parse OCSP request blob", e);
		}
	}
	
	public static OCSPResp parseOcspResponse(byte[] blob) {
		try {
			return new OCSPResp(blob);
		} catch (IOException e) {
			throw new RuntimeException("Unable parse OCSP response blob", e);
		}
	}
	
	public static String generateOcspRequestID(byte[] blob) {
		OCSPReq req = parseOcspRequest(blob);
		byte[] certId = null;
		try {
			certId = req.getRequestList()[0].getCertID().toASN1Primitive().getEncoded();
		} catch (IOException e) {
			throw new RuntimeException("Unable parse OCSP request blob", e);
		}
		
		DigestUtils digest = new DigestUtils(MessageDigestAlgorithms.SHA_256);
		return digest.digestAsHex(certId);
	}

	public static byte[] generateOCSPRequest(X509Certificate issuer, X509Certificate cert, byte[] nonce) {
			
		
		X509CertificateHolder issuerCert = null;
		try {
			issuerCert = new X509CertificateHolder(issuer.getEncoded());
		} catch (CertificateEncodingException | IOException e) {
			throw new RuntimeException("Unable to create X509CertificateHolder instance", e);
		}
		
		CertificateID certId = null;
		try {
			DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().build();
			certId = new CertificateID(digCalcProv.get(CertificateID.HASH_SHA1), issuerCert, cert.getSerialNumber());
		} catch (OperatorCreationException|OCSPException e) {
			throw new RuntimeException("Unable to create CertificateID", e);
		} 
		
		OCSPReqBuilder orb = new OCSPReqBuilder();
		orb.addRequest(certId);
		
		if(nonce != null) {
			orb.setRequestExtensions(new Extensions(
				 new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce,
				 false, new DEROctetString(nonce))));
		}
		
		try {
			return orb.build().getEncoded();
		} catch (IOException | OCSPException e) {
			throw new RuntimeException("Unable to encode request", e);
		}
		
	}
	
	public static byte[] createOcspNonce(X509Certificate cert) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(cert.getIssuerX500Principal().getEncoded());
			bos.write(cert.getSerialNumber().toByteArray());
			bos.write(("" + System.identityHashCode(cert)).getBytes()); // simple instance random
		} catch(IOException e) {
			throw new RuntimeException("buffer fill error", e);
		}
		
		DigestUtils digest = new DigestUtils(MessageDigestAlgorithms.SHA_256);
		byte[] out = digest.digest(bos.toByteArray());

		return Arrays.copyOfRange(out, 0, 20);
	}
	
}
