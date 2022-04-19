package com.utils.releaseshelper.validation;

/**
 * Internal exception for validation errors
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String validationMessage) {
		
		super(validationMessage);
	}
}
