package com.utils.releaseshelper.model.error;

/**
 * An Exception for any validation error
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String validationMessage) {
		
		super(validationMessage);
	}
}
