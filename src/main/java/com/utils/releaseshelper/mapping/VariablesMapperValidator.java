package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.properties.ValueDefinitionProperty;
import com.utils.releaseshelper.model.properties.VariableDefinitionProperty;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates variable definition properties
 */
@UtilityClass
public class VariablesMapperValidator {

	public static List<ValueDefinition> mapAndValidateValueDefinitions(List<ValueDefinitionProperty> valueDefinitionProperties) {
		
		ValidationUtils.notEmpty(valueDefinitionProperties, "At least one value definition should be defined");

		List<ValueDefinition> valueDefinitions = new ArrayList<>();
		
		for(int i = 0; i < valueDefinitionProperties.size(); i++) {
			
			ValueDefinitionProperty valueDefinitionProperty = valueDefinitionProperties.get(i);
			
			try {
				
				valueDefinitions.add(mapAndValidateValueDefinition(valueDefinitionProperty));
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid value definition at index " + i + " -> " + e.getMessage());
			}
		}
		
		return valueDefinitions;
	}
	
	public static ValueDefinition mapAndValidateValueDefinition(ValueDefinitionProperty valueDefinitionProperty) {

		ValueDefinition valueDefinition = new ValueDefinition();
		mapAndValidateCommonValueDefinition(valueDefinitionProperty, valueDefinition);
		return valueDefinition;
	}

	public static List<VariableDefinition> mapAndValidateVariableDefinitions(List<VariableDefinitionProperty> variableDefinitionProperties) {
		
		ValidationUtils.notEmpty(variableDefinitionProperties, "At least one variable definition should be defined");

		List<VariableDefinition> variableDefinitions = new ArrayList<>();
		
		for(int i = 0; i < variableDefinitionProperties.size(); i++) {
			
			VariableDefinitionProperty variableDefinitionProperty = variableDefinitionProperties.get(i);
			
			try {
				
				variableDefinitions.add(mapAndValidateVariableDefinition(variableDefinitionProperty));
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid variable definition at index " + i + " -> " + e.getMessage());
			}
		}
		
		return variableDefinitions;
	}
	
	public static VariableDefinition mapAndValidateVariableDefinition(VariableDefinitionProperty variableDefinitionProperty) {

		VariableDefinition variableDefinition = new VariableDefinition();
		
		mapAndValidateCommonValueDefinition(variableDefinitionProperty, variableDefinition);
		
		String key = ValidationUtils.notBlank(variableDefinitionProperty.getKey(), "Variable definition has no key");
		variableDefinition.setKey(key);
		
		return variableDefinition;
	}

	private static void mapAndValidateCommonValueDefinition(ValueDefinitionProperty valueDefinitionProperty, ValueDefinition valueDefinition) {
		
		String value = valueDefinitionProperty.getValue();
		Boolean askMe = valueDefinitionProperty.getAskMe();
		Boolean removeWhitespace = valueDefinitionProperty.getRemoveWhitespace();

		boolean hasFixedValue = !StringUtils.isBlank(value);
		boolean hasPromptValue = askMe != null && askMe;

		if(!(hasFixedValue ^ hasPromptValue)) {
		
			throw new ValidationException("Value definition must have EXACTLY ONE of these: a fixed value or a prompt value");
		}
		
		valueDefinition.setValue(value);
		valueDefinition.setAskMe(askMe != null && askMe);
		valueDefinition.setRemoveWhitespace(removeWhitespace != null && removeWhitespace);
	}
}
