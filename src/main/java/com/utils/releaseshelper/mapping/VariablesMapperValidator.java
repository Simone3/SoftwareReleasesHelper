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
	
	private static final String REMOVE_WHITESPACE_OPTION = "remove-whitespace";
	private static final Pattern ASK_ME_REGEX = Pattern.compile("\\s*\\{ask-me(\\s*,.*?)?\\}\\s*");

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
			
			String optionsString = askMeMatcher.group(1);
			if(!StringUtils.isBlank(optionsString)) {
				
				String[] options = optionsString.split(",");
				for(int i = 1; i < options.length; i++) {
					
					String option = options[i].trim();
					
					switch(option) {
					
						case REMOVE_WHITESPACE_OPTION:
							removeWhitespace = true;
							break;
						
						default:
							throw new ValidationException("Unknown \"ask-me\" option: " + option);
					}
				}
			}
		}
		else {
			
			actualValue = value;
		}

		ValueDefinition valueDefinition = new ValueDefinition();
		valueDefinition.setValue(actualValue);
		valueDefinition.setAskMe(askMe);
		valueDefinition.setRemoveWhitespace(removeWhitespace);		
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
		variableDefinition.setValue(variableValue);
		return variableDefinition;
	}
}