package com.utils.releaseshelper.mapping.domain;

import java.util.ArrayList;
import java.util.List;

import com.utils.releaseshelper.model.domain.OperatingSystemCommand;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.properties.OperatingSystemCommandProperty;
import com.utils.releaseshelper.utils.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates Jenkins properties
 */
@UtilityClass
public class OperatingSystemMapperValidator {

	public static List<OperatingSystemCommand> mapAndValidateOperatingSystemCommands(List<OperatingSystemCommandProperty> commandProperties) {
		
		List<OperatingSystemCommand> commands = new ArrayList<>();
		
		ValidationUtils.notEmpty(commandProperties, "There must be at least one OS command");
		
		int i = 0;
		for(OperatingSystemCommandProperty commandProperty: commandProperties) {
			
			try {
				
				OperatingSystemCommand command = mapAndValidateOperatingSystemCommand(commandProperty);
				commands.add(command);
				i++;
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid OS command definition at index " + i + " -> " + e.getMessage(), e);
			}
		}
		
		return commands;
	}

	private static OperatingSystemCommand mapAndValidateOperatingSystemCommand(OperatingSystemCommandProperty commandProperty) {
		
		String command = ValidationUtils.notBlank(commandProperty.getCommand(), "Command is empty");
		Boolean suppressOutput = commandProperty.getSuppressOutput();
		
		return new OperatingSystemCommand(command, suppressOutput != null && suppressOutput);
	}
}
