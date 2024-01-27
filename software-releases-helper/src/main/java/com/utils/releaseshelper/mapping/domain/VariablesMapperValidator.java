package com.utils.releaseshelper.mapping.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.domain.VariableDefinition;
import com.utils.releaseshelper.model.domain.VariableDefinitionType;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.properties.VariableDefinitionProperty;
import com.utils.releaseshelper.model.properties.VariableDefinitionTypeProperty;
import com.utils.releaseshelper.utils.ValidationUtils;
import com.utils.releaseshelper.utils.VariableUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates variable definition properties
 */
@UtilityClass
public class VariablesMapperValidator {
	
	public static List<VariableDefinition> mapAndValidateVariableDefinitions(List<VariableDefinitionProperty> variableProperties) {
		
		ValidationUtils.notEmpty(variableProperties, "At least one variable should be defined");

		Set<String> variableKeys = new HashSet<>();
		
		List<VariableDefinition> variableDefinitions = new ArrayList<>();
		
		int i = 0;
		for(VariableDefinitionProperty variableProperty: variableProperties) {
			
			try {
				
				VariableDefinition variableDefinition = mapAndValidateVariableDefinition(variableProperty);

				String variableKeyProperty = variableProperty.getKey();
				if(variableKeys.contains(variableKeyProperty)) {
					
					throw new ValidationException("Another variable has the \"" + variableKeyProperty + "\" key");
				}
				variableKeys.add(variableKeyProperty);
				
				variableDefinitions.add(variableDefinition);
				
				i++;
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid variable definition at index " + i + " -> " + e.getMessage(), e);
			}
		}
		
		return variableDefinitions;
	}
	
	public static VariableDefinition mapAndValidateVariableDefinition(VariableDefinitionProperty variableDefinitionProperty) {
		
		ValidationUtils.notNull(variableDefinitionProperty, "Variable has no definition");
		
		VariableDefinitionTypeProperty typeProperty = ValidationUtils.notNull(variableDefinitionProperty.getType(), "Variable has no type");
		
		switch(typeProperty) {
		
			case STATIC:
				return mapAndValidateStaticVariableDefinition(variableDefinitionProperty);
				
			case TEXT:
				return mapAndValidateTextVariableDefinition(variableDefinitionProperty);
				
			case FREE_SELECT:
				return mapAndValidateSelectVariableDefinition(variableDefinitionProperty, VariableDefinitionType.FREE_SELECT);
			
			case STRICT_SELECT:
				return mapAndValidateSelectVariableDefinition(variableDefinitionProperty, VariableDefinitionType.STRICT_SELECT);
				
			default:
				throw new ValidationException("Variable has an unknown type: " + typeProperty);
		}
	}

	private static VariableDefinition mapAndValidateGenericVariableDefinition(VariableDefinitionProperty variableDefinitionProperty, VariableDefinitionType type) {
		
		String key = ValidationUtils.notBlank(variableDefinitionProperty.getKey(), "Variable has no key");
		Boolean removeWhitespace = variableDefinitionProperty.getRemoveWhitespace();
		
		VariableDefinition variableDefinition = new VariableDefinition();
		
		variableDefinition.setType(type);
		variableDefinition.setKey(key);
		variableDefinition.setRemoveWhitespace(removeWhitespace != null && removeWhitespace);
		
		return variableDefinition;
	}	

	private static VariableDefinition mapAndValidateStaticVariableDefinition(VariableDefinitionProperty variableDefinitionProperty) {

		VariableDefinition variableDefinition = mapAndValidateGenericVariableDefinition(variableDefinitionProperty, VariableDefinitionType.STATIC);
		
		String value = ValidationUtils.notBlank(variableDefinitionProperty.getValue(), "Static variable definition has no value");
		value = VariableUtils.handleWhitespace(variableDefinition, value);
				
		variableDefinition.setValue(value);
		
		return variableDefinition;
	}	

	private static VariableDefinition mapAndValidateTextVariableDefinition(VariableDefinitionProperty variableDefinitionProperty) {

		VariableDefinition variableDefinition = mapAndValidateGenericVariableDefinition(variableDefinitionProperty, VariableDefinitionType.TEXT);
		
		String defaultValue = variableDefinitionProperty.getValue();
		defaultValue = VariableUtils.handleWhitespace(variableDefinition, defaultValue);
		
		variableDefinition.setValue(defaultValue);
		
		return variableDefinition;
	}	

	private static VariableDefinition mapAndValidateSelectVariableDefinition(VariableDefinitionProperty variableDefinitionProperty, VariableDefinitionType type) {

		VariableDefinition variableDefinition = mapAndValidateGenericVariableDefinition(variableDefinitionProperty, type);
		
		String defaultValue = variableDefinitionProperty.getValue();
		
		List<String> options = ValidationUtils.notEmpty(variableDefinitionProperty.getOptions(), "Select variable definition has no options");
		options = ValidationUtils.noneBlank(options, "Select variable definition has empty options");
		options = options.stream().map(option -> VariableUtils.handleWhitespace(variableDefinition, option)).collect(Collectors.toList());
		if(!StringUtils.isBlank(defaultValue)) {
			
			defaultValue = VariableUtils.handleWhitespace(variableDefinition, defaultValue);
			ValidationUtils.contains(options, defaultValue, "Select variable definition has a default value that is not one of the options");
		}
		
		variableDefinition.setOptions(options);
		variableDefinition.setValue(defaultValue);
		
		return variableDefinition;
	}
}
