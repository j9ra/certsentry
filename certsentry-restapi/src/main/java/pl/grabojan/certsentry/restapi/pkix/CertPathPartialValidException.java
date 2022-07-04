package pl.grabojan.certsentry.restapi.pkix;

public class CertPathPartialValidException extends CertPathFailureException {

	private static final long serialVersionUID = 1L;

	public CertPathPartialValidException() {
	}

	public CertPathPartialValidException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CertPathPartialValidException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertPathPartialValidException(String message) {
		super(message);
	}

	public CertPathPartialValidException(Throwable cause) {
		super(cause);
	}

}
