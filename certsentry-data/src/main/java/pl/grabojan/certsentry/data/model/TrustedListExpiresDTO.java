package pl.grabojan.certsentry.data.model;

import java.util.Date;

import lombok.Value;

@Value
public class TrustedListExpiresDTO {

	String territory;
	Date nextUpdate;
	Date lastCheck;
	Boolean isValid;

}
