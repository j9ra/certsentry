package pl.grabojan.certsentry.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpResource {
		
	private RestTemplate restTemplate;
	
	public HttpResource(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}
	
	public Optional<byte[]> post(String uri, byte[] body) {
		return request(HttpMethod.POST,uri,body);
	}
	
	public Optional<byte[]> get(String uri) {
		return request(HttpMethod.GET,uri,null);
	}
	
	private Optional<byte[]> request(HttpMethod method, String uri, byte[] body) {
		
		log.debug("Resource {} for uri location: [{}]", method, uri);
		
		URI location = null;
		try {
			location = new URI(uri);
		} catch (URISyntaxException e) {
			log.error("Failed to parse URI", e);
			throw new RuntimeException("Invalid uri syntax", e);
		}
		
		ResponseEntity<ByteArrayResource> ret = null;
		try {
			ret = restTemplate.execute(location,
				method, 
				(body != null) ? 
					(r) -> {
						r.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
						r.getBody().write(body); 
					} : null, 
				restTemplate.responseEntityExtractor(ByteArrayResource.class));
		
		} catch(HttpStatusCodeException e) {
			if(e.getRawStatusCode() == 404) { // quiet please! 
				log.warn("Resource not found {}", uri);
			} else if(e.getRawStatusCode() == 403) { // quiet please! 
				log.warn("Resource not allowed {}", uri);
			} else {
				log.error("Request failed for uri: " + uri, e);
			}
			return Optional.empty();
		} catch(RestClientException e) {
			log.error("Request failed for uri: " + uri, e);
			return Optional.empty();
		}
		
		if(!ret.getStatusCode().is2xxSuccessful()) {
			log.error("Unsuccessful request - response code {}", ret.getStatusCode().toString());
			return Optional.empty();
		} else {
			log.debug("Successful request - response contentType: {}, length: {}",
					ret.getHeaders().getContentType(),
					ret.getHeaders().getContentLength());
			return Optional.of(ret.getBody().getByteArray());
		}
		
	}

}
