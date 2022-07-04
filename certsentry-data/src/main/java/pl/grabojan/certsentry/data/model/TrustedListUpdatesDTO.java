package pl.grabojan.certsentry.data.model;

import java.util.Date;

import lombok.Value;

@Value
public class TrustedListUpdatesDTO {

	Long updateId;
	String territory;	
	Date lastUpdate;
	Date listIssue;
	UpdateStatus updateStatus;
	Integer errorCode;
}
