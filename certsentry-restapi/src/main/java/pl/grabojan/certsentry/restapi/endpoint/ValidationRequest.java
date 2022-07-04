package pl.grabojan.certsentry.restapi.endpoint;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class ValidationRequest {

	@NotEmpty(message = "Certificate is required - argument name - cert")
	private String cert;
	
	private String profile;
}
