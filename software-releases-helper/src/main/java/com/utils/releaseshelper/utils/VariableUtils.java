package com.utils.releaseshelper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.domain.VariableDefinition;
import com.utils.releaseshelper.model.misc.KeyValuePair;

import lombok.experimental.UtilityClass;

/**
 * Helper utils for variables
 */
@UtilityClass
public class VariableUtils {

	public static String replacePlaceholders(String sourceString, List<KeyValuePair> placeholderValues) {
		
		if(StringUtils.isBlank(sourceString)) {
			
			return sourceString;
		}
		
		if(CollectionUtils.isEmpty(placeholderValues)) {
			
			return sourceString;
		}
		
		// Very simple replace algorithm because implementation must be the same between front-end (preview) and back-end (real computation), but they could be both evolved for better performance
		String result = sourceString;
		for(KeyValuePair variableValue: placeholderValues) {
			
			if(variableValue.getValue() != null) {
				result = result.replace("#[" + variableValue.getKey() + "]", variableValue.getValue());
			}
		}
		return result;
	}	
	
	public List<KeyValuePair> getVariableValues(List<VariableDefinition> sourceVariables, List<KeyValuePair> userValues) {
		
		if(CollectionUtils.isEmpty(sourceVariables)) {
			
			return List.of();
		}
		else {
			
			// Build a map of the user-provided values
			Map<String, String> userValuesMap = new HashMap<>();
			if(userValues != null) {
				
				for(KeyValuePair userValue: userValues) {
					
					userValuesMap.put(userValue.getKey(), userValue.getValue());
				}
			}
			
			List<KeyValuePair> result = new ArrayList<>();
			
			// Loop the original (definition) variables
			for(VariableDefinition variable: sourceVariables) {

				String key = variable.getKey();
				switch(variable.getType()) {
				
					// For static variables, just pick the definition value
					case STATIC: {
						String staticValue = variable.getValue();
						result.add(new KeyValuePair(key, staticValue));
						break;
					}
					
					// For text variables, get the user-provided value
					case TEXT:
					case FREE_SELECT: {
						String userValue = userValuesMap.get(key);
						if(!StringUtils.isBlank(userValue)) {
							
							userValue = handleWhitespace(variable, userValue);
							result.add(new KeyValuePair(key, userValue));
						}
						break;
					}
					
					// For strict-select variables, get the user-provided value but first validate if it's a valid option
					case STRICT_SELECT: {
						String userValue = userValuesMap.get(key);
						if(!StringUtils.isBlank(userValue)) {
							
							validateSelectVariableValue(variable, userValue);
							result.add(new KeyValuePair(key, userValue));
						}
						break;
					}
					
					default: {
						throw new IllegalStateException("Unmapped variable type: " + variable.getType());
					}
				}
			}
			
			return result;
		}
	}
	
	public String handleWhitespace(VariableDefinition variable, String value) {
		
		if(variable.isRemoveWhitespace()) {
			
			return removeWhitespace(value);
		}
		else {
			
			return value;
		}
	}
	
	private void validateSelectVariableValue(VariableDefinition variable, String value) {
		
		for(String option: variable.getOptions()) {
			
			if(option.equals(value)) {
				
				return;
			}
		}
		
		throw new IllegalStateException("Value " + value + " is not a valid option for select variable");
	}
	
	private String removeWhitespace(String text) {
		
		if(text == null) {
			
			return null;
		}
		
		return text
			.replaceAll("\\s+", "")
			.replace("\u200B", "");
	}
}
