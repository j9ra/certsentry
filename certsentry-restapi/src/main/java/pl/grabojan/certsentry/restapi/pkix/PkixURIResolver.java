package pl.grabojan.certsentry.restapi.pkix;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import pl.grabojan.certsentry.util.CertificateServiceFailureException;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;

@RequiredArgsConstructor
public class PkixURIResolver {

	private final HttpResource httpResource;
	private final CertificateServiceHelper certificateServiceHelper;
	
	
	public X509Certificate getCertificate(String uri) {
		
		Optional<byte[]> resp = httpResource.get(uri);
		byte[] certBlob = resp.orElseThrow(() -> new RuntimeException("Unable to retrive Certificate"));
		
		X509Certificate cert = null;
		try {
			cert = certificateServiceHelper.parseCertificate(certBlob);
		} catch(CertificateServiceFailureException e) {
			// no action, skip invalid file 
		}
		
		if(cert != null) {
			return cert;
		}
			
		try {
			List<X509Certificate> certPath = certificateServiceHelper.parseCertificatePath(certBlob);
			if(certPath.size() > 0) {
				cert = certPath.get(0);
			}
		} catch(CertificateServiceFailureException e) {
			// no action, skip invalid file
		}
		
		if(cert == null) {
			throw new UnresolvedResourceException("Unable to retrive Certificate from: " + uri);
		}
		
		return cert;
	}
	
	public X509CRL getCrl(String uri) {
		
		Optional<byte[]> resp = httpResource.get(uri);
		byte[] crlBlob = resp.orElseThrow(() -> new UnresolvedResourceException("Unable to retrive CRL from: " + uri));
		
		return certificateServiceHelper.parseCRL(crlBlob);
	}
	
	public byte[] resolveWithGet(String uri) {
		
		Optional<byte[]> resp = httpResource.get(uri);
		byte[] out = resp.orElseThrow(() -> new UnresolvedResourceException("Resolve with GET failed " + uri));
		return out;
	}
	
	public byte[] resolveWithPost(String uri, byte[] body) {
	
		Optional<byte[]> resp = httpResource.post(uri, body);
		byte[] out = resp.orElseThrow(() -> new UnresolvedResourceException("Resolve with POST failed " + uri));
		return out;
	}
	
}
