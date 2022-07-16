package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates variable/value definition properties
 */
@UtilityClass
public class VariablesMapperValidator {
	
	private static int valueDefinitionCurrentId = 0;
	
	private static final String REMOVE_WHITESPACE_OPTION = "remove-whitespace";
	private static final String DEFAULT_VALUE_OPTION = "default";
	private static final Pattern ASK_ME_REGEX = Pattern.compile("\\s*\\{\\s*ask-me(\\s*,.*?)?\\s*\\}\\s*");

	public static List<ValueDefinition> mapAndValidateValueDefinitions(List<String> valueDefinitionProperties) {
		
		ValidationUtils.notEmpty(valueDefinitionProperties, "At least one value definition should be defined");

		List<ValueDefinition> valueDefinitions = new ArrayList<>();
		
		for(int i = 0; i < valueDefinitionProperties.size(); i++) {
			
			String valueDefinitionProperty = valueDefinitionProperties.get(i);
			
			try {
				
				valueDefinitions.add(mapAndValidateValueDefinition(valueDefinitionProperty));
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid value definition at index " + i + " -> " + e.getMessage());
			}
		}
		
		return valueDefinitions;
	}
	
	public static ValueDefinition mapAndValidateValueDefinition(String valueDefinitionProperty) {

		String value = ValidationUtils.notBlank(valueDefinitionProperty, "No value provided");
		
		Matcher askMeMatcher = ASK_ME_REGEX.matcher(valueDefinitionProperty);
		boolean isAskMeValue = askMeMatcher.find();
		
		String actualValue;
		boolean askMe = false;
		boolean removeWhitespace = false;
		
		if(isAskMeValue) {
			
			actualValue = null;
			askMe = true;
			
			// Check if there are "ask-me" options
			String optionsString = askMeMatcher.group(1);
			if(!StringUtils.isBlank(optionsString)) {
				
				// Multiple options are divided by commas
				String[] options = optionsString.split(",");
				for(int i = 1; i < options.length; i++) {
					
					// Some options may have a key-value format divided by colon (just the first colon is considered)
					String[] optionKeyValue = options[i].split(":", 2);
					String optionKey = optionKeyValue[0].trim();
					String optionValue = optionKeyValue.length > 1 ? optionKeyValue[1].trim() : null;

					switch(optionKey) {
					
						// All whitespace will be removed after user prompt
						case REMOVE_WHITESPACE_OPTION:
							removeWhitespace = true;
							break;
						
						// User will be prompted with a default value
						case DEFAULT_VALUE_OPTION:
							actualValue = optionValue;
							break;
						
						default:
							throw new ValidationException("Unknown \"ask-me\" option: " + optionKey);
					}
				}
			}
		}
		else {
			
			actualValue = value;
		}

		ValueDefinition valueDefinition = new ValueDefinition();
		valueDefinition.setId(valueDefinitionCurrentId);
		valueDefinition.setStaticContent(actualValue);
		valueDefinition.setAskMe(askMe);
		valueDefinition.setRemoveWhitespace(removeWhitespace);	
		
		valueDefinitionCurrentId++;
		
		return valueDefinition;
	}

	public static List<VariableDefinition> mapAndValidateVariableDefinitions(Map<String, String> variableDefinitionProperties) {
		
		ValidationUtils.notEmpty(variableDefinitionProperties, "At least one variable should be defined");

		List<VariableDefinition> variableDefinitions = new ArrayList<>();
		
		int i = 0;
		for(Entry<String, String> entry: variableDefinitionProperties.entrySet()) {

			String variableKeyProperty = entry.getKey();
			String variableValueProperty = entry.getValue();
			
			try {
				
				VariableDefinition variableDefinition = mapAndValidateVariableDefinition(variableKeyProperty, variableValueProperty);
				variableDefinitions.add(variableDefinition);
				i++;
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid variable definition at index " + i + " -> " + e.getMessage());
			}
		}
		
		return variableDefinitions;
	}
	
	public static VariableDefinition mapAndValidateVariableDefinition(String variableKeyProperty, String variableValueProperty) {
		
		String variableKey = ValidationUtils.notBlank(variableKeyProperty, "Variable has no key");
		ValueDefinition variableValue = mapAndValidateValueDefinition(variableValueProperty);
		
		VariableDefinition variableDefinition = new VariableDefinition();
		variableDefinition.setKey(variableKey);
		variableDefinition.setValueDefinition(variableValue);
		return variableDefinition;
	}
}
