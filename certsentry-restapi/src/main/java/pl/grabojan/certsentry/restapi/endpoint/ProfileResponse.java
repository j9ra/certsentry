package pl.grabojan.certsentry.restapi.endpoint;

import lombok.Data;

@Data
public class ProfileResponse {

	private String name;
	private String territory;
	private String provider;
	private String service_info;
	
}
