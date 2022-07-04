package pl.grabojan.certsentry.admin.rest.dto;


import lombok.Data;

@Data
public class User {

	private String username; 
	private String password;
	private Boolean enabled;
	
	private String roles;
	
	private String appname;
	private String description;
	private String apikey;
}
