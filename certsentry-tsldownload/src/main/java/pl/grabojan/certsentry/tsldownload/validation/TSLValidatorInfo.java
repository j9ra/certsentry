package pl.grabojan.certsentry.tsldownload.validation;

import lombok.Data;

@Data
public class TSLValidatorInfo {

	private final Status status;
	private final Stage failedStage;
	private final String errorMessage;
	private final int errorCode;
	
	public enum Status {
		OK, FAILED
	}

	public enum Stage {
		FORMAT, SCHEMA, SIGNATURE,
		LIST_SUPPORT, NONE
	};

}
	