package com.utils.releaseshelper.model.view.cli;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A container for parsing results
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParseResult<V> {
	
	private final boolean success;
	private final V value;
	private final String message;
	
	public static <T> ParseResult<T> fail(String message) {
		
		return new ParseResult<>(false, null, message);
	}

	public static <T> ParseResult<T> ok(T value) {
		
		return new ParseResult<>(true, value, null);
	}
}
