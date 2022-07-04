package pl.grabojan.certsentry.restapi.pkix;

public class UnresolvedResourceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnresolvedResourceException() {
		super();
	}

	public UnresolvedResourceException(String message) {
		super(message);
	}

	public UnresolvedResourceException(Throwable cause) {
		super(cause);
	}

	public UnresolvedResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnresolvedResourceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
