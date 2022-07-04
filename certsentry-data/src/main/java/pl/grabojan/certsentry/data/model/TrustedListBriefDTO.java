package pl.grabojan.certsentry.data.model;

import java.util.Date;

import lombok.Value;

@Value
public class TrustedListBriefDTO {

	String territory;
	String operatorName;
	Long sequenceNumber;
	Date nextUpdate;
	Date listIssue;
	Date lastCheck;
	Boolean isValid;

}
