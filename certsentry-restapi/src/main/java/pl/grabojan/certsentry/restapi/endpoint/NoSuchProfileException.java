package pl.grabojan.certsentry.restapi.endpoint;

public class NoSuchProfileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchProfileException() {
		super();
	}

	public NoSuchProfileException(String message) {
		super(message);
	}

	public NoSuchProfileException(Throwable cause) {
		super(cause);
	}

	public NoSuchProfileException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchProfileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
