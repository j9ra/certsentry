package pl.grabojan.certsentry.restapi.pkix;

public class CertPathFailureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CertPathFailureException() {
		super();
	}

	public CertPathFailureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CertPathFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertPathFailureException(String message) {
		super(message);
	}

	public CertPathFailureException(Throwable cause) {
		super(cause);
	}

}
