package com.utils.releaseshelper.validation;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

/**
 * Helper util for validation
 */
@UtilityClass
public class ValidationUtils {

	public static <T> List<T> notEmpty(List<T> value, String message) {
		
		if(value == null || value.isEmpty()) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}

	public static <K, V> Map<K, V> notEmpty(Map<K, V> value, String message) {
		
		if(value == null || value.isEmpty()) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
	
	public String notBlank(String value, String message) {
		
		if(StringUtils.isBlank(value)) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
	
	public <T> T notNull(T value, String message) {
		
		if(value == null) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
	
	public int integer(String value, String message) {
		
		try {
			
			return Integer.parseInt(value);
		}
		catch(Exception e) {
			
			throw new ValidationException(message);
		}
	}
	
	public int positive(Integer value, String message) {
		
		if(value == null || value <= 0) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
	
	public int range(Integer value, Integer from, Integer to, String message) {
		
		if(value == null || (from != null && value < from) || (to != null && value > to)) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
	
	public Boolean isTrue(Boolean value, String message) {
		
		if(value == null || !value) {
			
			throw new ValidationException(message);
		}
		
		return value;
	}
}
