package com.utils.releaseshelper.model.error;

/**
 * An Exception for a Git unathorized error
 */
public class GitUnauthorizedException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public GitUnauthorizedException() {
		super();
	}

	public GitUnauthorizedException(String s) {
		super(s);
	}

	public GitUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitUnauthorizedException(Throwable cause) {
		super(cause);
	}
}
