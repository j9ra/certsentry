package pl.grabojan.certsentry.util;

public class CertificateServiceFailureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CertificateServiceFailureException() {
		super();
	}

	public CertificateServiceFailureException(String message) {
		super(message);
	}

	public CertificateServiceFailureException(Throwable cause) {
		super(cause);
	}

	public CertificateServiceFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertificateServiceFailureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
