package pl.grabojan.certsentry.restapi.pkix;

public class CertPathNotFoundException extends CertPathFailureException {

	private static final long serialVersionUID = 1L;

	public CertPathNotFoundException() {
	}

	public CertPathNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CertPathNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertPathNotFoundException(String message) {
		super(message);
	}

	public CertPathNotFoundException(Throwable cause) {
		super(cause);
	}

}
