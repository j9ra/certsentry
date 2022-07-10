package pl.grabojan.certsentry.restapi.endpoint;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.Profile;
import pl.grabojan.certsentry.data.service.EventLogDataService;
import pl.grabojan.certsentry.data.service.ProfileDataService;
import pl.grabojan.certsentry.restapi.pkix.CertPathNotFoundException;
import pl.grabojan.certsentry.restapi.pkix.CertPathNotValidException;
import pl.grabojan.certsentry.restapi.pkix.CertPathPartialValidException;
import pl.grabojan.certsentry.restapi.pkix.CertPathServiceHelper;
import pl.grabojan.certsentry.util.CertificateServiceHelper;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(path="/v1",produces="application/json")
public class CertsentryRestApiV1 {
	
	private final ProfileDataService profileDataService;
	private final CertificateServiceHelper certificateServiceHelper;
	private final CertPathServiceHelper certPathServiceHelper;
	private final EventLogDataService eventLogDataService;

	@GetMapping("/profile/{name}")
	public ResponseEntity<ProfileResponse> getProfile(
			@PathVariable 
			@Size(min=1,max=20,message = "Param Name must have size in (1,20)")
			String name) {
		
		log.debug("getProfile [{}]", name);
		
		Optional<Profile> profile = profileDataService.getProfile(name);
		if(profile.isPresent()) {
			return new ResponseEntity<>(toProfileResponse(profile.get()), 
					defaultHeaders(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(null, defaultHeaders(), HttpStatus.NOT_FOUND);
	}
	
	@GetMapping("/profile")
	public List<ProfileResponse> getProfiles() {
		
		List<ProfileResponse> profilesRet = profileDataService.
				getAllProfiles().
				stream().
				map(p -> toProfileResponse(p)).
				collect(Collectors.toList());
			
		return profilesRet;
	}
	
	@PostMapping("/validation")
	public ValidationResponse validation(@Valid @RequestBody ValidationRequest request) {
		
		log.debug("ValidationRequest: {}" , request);
		
		X509Certificate cert = certificateServiceHelper.parseCertificate(request.getCert());
		String profileName = request.getProfile();
		Profile profile = null;
		if(profileName != null) {
			profile = profileDataService.getProfile(profileName).
					orElseThrow(() -> new NoSuchProfileException("Profile: " + profileName + " not found"));
		}
						
		ValidationResponse resp = new ValidationResponse();
		resp.setRef(UUID.randomUUID().toString());
		
		try {
			List<X509Certificate> path = certPathServiceHelper.buildPathAndValidate(cert, profile);
			resp.setCertPath(toStringPath(path));
			resp.setStatus("VALID");
			resp.setReasonMessage("");		
				
		} catch (CertPathNotFoundException e) {
			log.error("CertPath failure", e);
			resp.setCertPath(toStringPath(Collections.singletonList(cert)));
			resp.setStatus("INVALID");
			resp.setReasonMessage(e.getMessage());
			
		} catch (CertPathPartialValidException e) { 
			log.error("CertPath build but partialy validated", e);
			
			resp.setCertPath(toStringPath(Collections.singletonList(cert)));
			resp.setStatus("INVALID");
			resp.setReasonMessage(e.getMessage() + 
					(e.getCause() != null ? e.getCause().getMessage() : ""));
			
		} catch (CertPathNotValidException e) {	
			log.error("CertPath build but not validated", e);
			
			resp.setStatus("INVALID");
			// resolve exception cause, if available
			if(e.getCause() != null && e.getCause() instanceof CertPathValidatorException) {
				CertPathValidatorException cpve = (CertPathValidatorException)e.getCause();
				resp.setCertPath(toStringPath(cpve.getCertPath()));
				resp.setReasonMessage(cpve.getReason().toString());
			} else {
				resp.setCertPath(toStringPath(Collections.singletonList(cert)));
				resp.setReasonMessage(e.getMessage());
			}
		} catch (Exception e) {
			log.error("CertPath server error", e);
			resp.setStatus("ERROR");
			resp.setReasonMessage("General Server Error");
		}
		
		// audit log
		if("VALID".equals(resp.getStatus())) {
			eventLogDataService.logInfoEvent("Validation", 
					createMsg(cert, resp.getRef(), "VALID"), getCurrentUser());
		} else {
			eventLogDataService.logErrorEvent("Validation", 
					createMsg(cert, resp.getRef(), resp.getStatus() + 
							" " + resp.getReasonMessage()), getCurrentUser());
		}
		
		return resp;
	}

	private HttpHeaders defaultHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setConnection("close");
		return headers;
	}
	
	private ProfileResponse toProfileResponse(Profile profile) {
		ProfileResponse p = new ProfileResponse();
		p.setName(profile.getName());
		p.setTerritory(profile.getTerritory());
		p.setProvider(profile.getProvider() != null ? profile.getProvider() : "" );
		p.setService_info(profile.getServiceInfo() != null ? profile.getServiceInfo() : "");
		return p;
	}
	
	private List<String> toStringPath(List<X509Certificate> path) {
		return path.stream().
				map(c -> certificateServiceHelper.certificateToString(c)).
				collect(Collectors.toList());
	}
	
	private List<String> toStringPath(CertPath cp) {
		List<X509Certificate> xCerts = cp.getCertificates().
				stream().
				map(c -> (X509Certificate)c).
				collect(Collectors.toList());
		return toStringPath(xCerts);
	}
	
	private String getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	private String createMsg(X509Certificate cert, String ref, String msg) {
	
		StringBuilder sb = new StringBuilder();
		sb.append("Reference").append('=').append(ref).append('|');
		sb.append("SerialNumber").append('=').append(cert.getSerialNumber().toString()).append('|');
		sb.append("Issuer").append('=').
			append(certificateServiceHelper.resolveX500Principal(cert.getIssuerX500Principal())).
			append('|');
		sb.append("Message").append('=').append(msg).append('|');
		
		return sb.toString();
	}
 	
}
