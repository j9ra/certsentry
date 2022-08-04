package pl.grabojan.certsentry.mock.ocsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;
import pl.grabojan.certsentry.util.OcspClientService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ResponderController {
	
	private final CertificateServiceHelper certServiceHelper;
	
	private final HttpResource httpResource;
	
	private final OcspMockProperties properties;
	
	private ConcurrentHashMap<String, byte[]> objectMap = new ConcurrentHashMap<>();
	
	@PostMapping
	public void ocsp(InputStream request, OutputStream response) {
		
		try {
			byte[] ocspRequest = request.readAllBytes();
			
			String genId = OcspClientService.generateOcspRequestID(ocspRequest);
			log.debug("OCSP request id: {}", genId);
			
			log.debug("Checking map for response");
			byte[] ocspResponse = objectMap.get(genId);
			log.debug("Is response found? {}", ocspResponse != null );
			
			try {
				log.debug("Delay of {} ms", properties.getDelay().toMillis());
				Thread.sleep(properties.getDelay().toMillis());
				log.debug("Delay done");
			} catch (InterruptedException e) {
				System.err.println("Interrupted!");
				e.printStackTrace();
			}
			
			if(ocspResponse != null) {
				response.write(ocspResponse);
				response.flush();
			}
			
		} catch (IOException e) {
			log.error("Ocsp mocking failed", e);
			return;
		}
		
	}
	
	@PutMapping(path = "/register")
	public @ResponseBody String registerCertificate(@RequestBody String certificate) {
	
		log.info("Certificate registration");
		log.debug("Received certificate value: [{}]", certificate);
		
		certificate = certificate
							.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "")
							.replaceAll("\\s+", "");
		
		log.debug("Certificate value to parse: [{}]", certificate);
		X509Certificate cert = certServiceHelper.parseCertificate(certificate);
		log.debug("Cert: [{}]", cert);
		
		String caIssuersURI = certServiceHelper.getCaIssuersURI(cert);
		log.debug("CA Issuers URI: [{}]", caIssuersURI);
		
		String ocspURI = certServiceHelper.getOcspResponderURI(cert);
		log.debug("OCSP Responder URI: [{}]", ocspURI);
		if(ocspURI == null) {
			log.error("No OCSP responder in cert!");
			return "Error: cert dont support OCSP mechanism";
		}
		
		log.debug("Downloading issuer CA certificate");
		Optional<byte[]> caCertificate = httpResource.get(caIssuersURI);
		if(caCertificate.isEmpty()) {
			return "Error: failed to download issuing CA cert";
		}
		
		log.debug("Got certificate");
		X509Certificate caCert = certServiceHelper.parseCertificate(caCertificate.get());
		log.debug("CA Cert: [{}]", caCert);
		
		
		log.debug("Query OCSP for revocation info for cert");
		
		long t1 = System.currentTimeMillis();
		
		byte[] ocspRequest = OcspClientService.generateOCSPRequest(caCert, cert, null);
		String genId = OcspClientService.generateOcspRequestID(ocspRequest);
		log.debug("OCSP request id: {}", genId);

		Optional<byte[]> ocspResponse = httpResource.post(ocspURI, ocspRequest);
		long t2 = System.currentTimeMillis() - t1;
		
		if(ocspResponse.isEmpty()) {
			return "Error: failed to query ocsp";
		}
		
		
		log.debug("Got OCSP response in {} ms", t2);
					 
		
		try {
			BasicOCSPResp bor = (BasicOCSPResp)OcspClientService.parseOcspResponse(ocspResponse.get()).getResponseObject();
			
			log.debug("OCSP response details - producedAt: {}, responderId: {}, thisUpdate: {}, nextUpdate: {}, ",
					bor.getProducedAt(), bor.getResponderId(), bor.getResponses()[0].getThisUpdate(),
					bor.getResponses()[0].getNextUpdate());
		} catch (OCSPException e) {
			log.error("Failed to parse OCSP response", e);
			return "Error: failed to parse OCSP response";
		}
				
		
		log.debug("Filling map with OCSP response");
		objectMap.put(genId, ocspResponse.get());
		
		
		return "OK";
	}
	
}
