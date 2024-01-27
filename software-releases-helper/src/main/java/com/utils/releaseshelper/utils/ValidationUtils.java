package com.utils.releaseshelper.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.error.ValidationException;

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

	public static List<String> noneBlank(List<String> value, String message) {

		if(value != null && !value.isEmpty()) {
			
			for(String element: value) {
				
				notBlank(element, message);
			}
		}
		
		return value;
	}

	public static Map<String, String> noneBlank(Map<String, String> value, String message) {
		
		if(value != null && !value.isEmpty()) {
			
			for(Entry<String, String> entry: value.entrySet()) {
				
				notBlank(entry.getKey(), message);
				notBlank(entry.getValue(), message);
			}
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
			
			throw new ValidationException(message, e);
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

	public static <T> void contains(List<T> value, T containedValue, String message) {
		
		notEmpty(value, message);
		
		for(T element: value) {
			
			if(element != null && element.equals(containedValue)) {
				
				return;
			}
		}
		
		throw new ValidationException(message);
	}
}
