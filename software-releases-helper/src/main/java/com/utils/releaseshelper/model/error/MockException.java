package com.utils.releaseshelper.model.error;

/**
 * An Exception for mock errors
 */
public class MockException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MockException() {
		super();
	}

	public MockException(String s) {
		super(s);
	}

	public MockException(String message, Throwable cause) {
		super(message, cause);
	}

	public MockException(Throwable cause) {
		super(cause);
	}
}
