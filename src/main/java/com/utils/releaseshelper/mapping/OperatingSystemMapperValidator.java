package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.process.OperatingSystemCommand;
import com.utils.releaseshelper.model.properties.GenericCommandProperty;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates operating system properties
 */
@UtilityClass
public class OperatingSystemMapperValidator {

	public static List<OperatingSystemCommand> mapAndValidateOperatingSystemCommands(List<GenericCommandProperty> operatingSystemCommandProperties) {
		
		ValidationUtils.notEmpty(operatingSystemCommandProperties, "At least one operating system command should be defined");
		
		List<OperatingSystemCommand> operatingSystemCommands = new ArrayList<>();
		
		for(int i = 0; i < operatingSystemCommandProperties.size(); i++) {

			GenericCommandProperty operatingSystemCommandProperty = operatingSystemCommandProperties.get(i);
			
			OperatingSystemCommand operatingSystemCommand;
			try {
				
				operatingSystemCommand = mapAndValidateOperatingSystemCommand(operatingSystemCommandProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid operating system command at index " + i + " -> " + e.getMessage());
			}

			operatingSystemCommands.add(operatingSystemCommand);
		}
		
		return operatingSystemCommands;
	}
	
	public static OperatingSystemCommand mapAndValidateOperatingSystemCommand(GenericCommandProperty operatingSystemCommandProperty) {
		
		ValidationUtils.notNull(operatingSystemCommandProperty, "No operating system command defined");
		
		String commandValueProperty = operatingSystemCommandProperty.getCommand();
		Boolean suppressOutput = operatingSystemCommandProperty.getSuppressOutput();
		
		ValueDefinition commandValue;
		try {
			
			commandValue = VariablesMapperValidator.mapAndValidateValueDefinition(commandValueProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid operating system command -> " + e.getMessage());
		}
		
		OperatingSystemCommand operatingSystemCommand = new OperatingSystemCommand();
		operatingSystemCommand.setCommand(commandValue);
		operatingSystemCommand.setSuppressOutput(suppressOutput != null && suppressOutput);
		return operatingSystemCommand;
	}
}
