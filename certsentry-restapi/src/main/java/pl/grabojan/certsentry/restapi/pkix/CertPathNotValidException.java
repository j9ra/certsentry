package pl.grabojan.certsentry.restapi.pkix;

public class CertPathNotValidException extends CertPathFailureException {

	private static final long serialVersionUID = 1L;
	
	public CertPathNotValidException() {
		
	}

	public CertPathNotValidException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CertPathNotValidException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertPathNotValidException(String message) {
		super(message);	
	}

	public CertPathNotValidException(Throwable cause) {
		super(cause);
	}

}
