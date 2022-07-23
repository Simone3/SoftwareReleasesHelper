package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.ValueDefinitionType;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.view.SelectOption;
import com.utils.releaseshelper.model.view.SimpleSelectOption;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates variable/value definition properties
 */
@UtilityClass
public class VariablesMapperValidator {
	
	private static int valueDefinitionCurrentId = 0;
	
	private static final Pattern ASK_ME_REGEX = Pattern.compile("\\s*\\{\\s*ask-me(\\s*,.*?)?\\s*\\}\\s*");
	
	private static final String REMOVE_WHITESPACE_SPECIFIER = "remove-whitespace";
	private static final String DEFAULT_VALUE_SPECIFIER     = "default";
	private static final String SELECT_SPECIFIER            = "select-options";
	
	private static final Set<String> ALL_SPECIFIERS = Set.of(REMOVE_WHITESPACE_SPECIFIER, DEFAULT_VALUE_SPECIFIER, SELECT_SPECIFIER);

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
		
		ValueDefinition valueDefinition = new ValueDefinition();
		valueDefinition.setId(valueDefinitionCurrentId);
		valueDefinitionCurrentId++;
		
		if(isAskMeValue) {
			
			mapAndValidateDynamicValueDefinition(valueDefinition, askMeMatcher);
		}
		else {

			mapAndValidateStaticValueDefinition(value, valueDefinition);
		}
		
		return valueDefinition;
	}
	
	private static void mapAndValidateStaticValueDefinition(String value, ValueDefinition valueDefinition) {
		
		valueDefinition.setDefinitionType(ValueDefinitionType.STATIC);
		valueDefinition.setStaticContent(value);
		valueDefinition.setRemoveWhitespace(false);
		valueDefinition.setOptions(null);
	}
	
	private static void mapAndValidateDynamicValueDefinition(ValueDefinition valueDefinition, Matcher askMeMatcher) {
		
		// Check if there are "ask-me" specifiers
		String specifiersString = askMeMatcher.group(1);
		Map<String, String> specifiersMap = parseAskMeSpecifiers(specifiersString);
		
		// User will select from a pre-defined set of values
		boolean hasSelectOptions = specifiersMap.containsKey(SELECT_SPECIFIER);
		if(hasSelectOptions) {
			
			String selectOptions = ValidationUtils.notBlank(specifiersMap.get(SELECT_SPECIFIER), "Empty select options list");
			
			List<SelectOption> options = Stream.of(selectOptions.split("\\|")).map(option -> new SimpleSelectOption(option.trim())).collect(Collectors.toList());

			valueDefinition.setDefinitionType(ValueDefinitionType.DYNAMIC_SELECT);
			valueDefinition.setOptions(options);
		}
		else {
			
			valueDefinition.setDefinitionType(ValueDefinitionType.DYNAMIC_TEXT);
			valueDefinition.setOptions(null);
		}
		
		// All whitespace will be removed after user prompt
		boolean isRemoveWhitespace = specifiersMap.containsKey(REMOVE_WHITESPACE_SPECIFIER);
		valueDefinition.setRemoveWhitespace(isRemoveWhitespace);
		
		// User will be prompted with a default value
		boolean hasDefaultValue = specifiersMap.containsKey(DEFAULT_VALUE_SPECIFIER);
		if(hasDefaultValue) {
			
			String defaultValue = ValidationUtils.notBlank(specifiersMap.get(DEFAULT_VALUE_SPECIFIER), "Empty default value");
			
			// Special default value if we also have select options
			if(hasSelectOptions) {
				
				int index = ValidationUtils.integer(defaultValue, "Non-numeric default value for select options");
				ValidationUtils.range(index, 0, valueDefinition.getOptions().size() - 1, "Out of bounds default value for select options");
			}
			
			valueDefinition.setStaticContent(defaultValue);
		}
		else {
			
			valueDefinition.setStaticContent(null);
		}
	}
	
	private static Map<String, String> parseAskMeSpecifiers(String specifiersString) {
		
		Map<String, String> specifiersMap = new HashMap<>();
		
		if(!StringUtils.isBlank(specifiersString)) {
			
			// Multiple specifiers are divided by commas
			String[] specifiers = specifiersString.split(",");
			for(int i = 1; i < specifiers.length; i++) {
				
				// Some specifiers may have a key-value format divided by colon (just the first colon is considered)
				String[] specifierKeyValue = specifiers[i].split(":", 2);
				String specifierKey = specifierKeyValue[0].trim();
				String specifierValue = specifierKeyValue.length > 1 ? specifierKeyValue[1].trim() : null;
				
				if(!ALL_SPECIFIERS.contains(specifierKey)) {
					
					throw new ValidationException("Unknown \"ask-me\" specifier: " + specifierKey);
				}
				
				specifiersMap.put(specifierKey, specifierValue);
			}
		}
		
		return specifiersMap;
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
