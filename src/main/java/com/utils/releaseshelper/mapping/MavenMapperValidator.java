package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.config.MavenConfig;
import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;
import com.utils.releaseshelper.model.properties.MavenCommandProperty;
import com.utils.releaseshelper.model.properties.MavenProperties;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates Maven properties
 */
@UtilityClass
public class MavenMapperValidator {

	public static MavenConfig mapAndValidateMavenConfig(MavenProperties mavenProperties) {
		
		ValidationUtils.notNull(mavenProperties, "No Maven properties are defined");
		
		String mavenHomeFolder = ValidationUtils.notBlank(mavenProperties.getMavenHomeFolder(), "Maven home folder is empty");
		
		MavenConfig mavenConfig = new MavenConfig();
		mavenConfig.setMavenHomeFolder(mavenHomeFolder);
		return mavenConfig;
	}
	
	public static List<MavenCommand> mapAndValidateMavenCommands(List<MavenCommandProperty> mavenCommandProperties) {
		
		ValidationUtils.notEmpty(mavenCommandProperties, "At least one Maven command should be defined");
		
		List<MavenCommand> mavenCommands = new ArrayList<>();
		
		for(int i = 0; i < mavenCommandProperties.size(); i++) {

			MavenCommandProperty mavenCommandProperty = mavenCommandProperties.get(i);
			
			MavenCommand mavenCommand;
			try {
				
				mavenCommand = mapAndValidateMavenCommand(mavenCommandProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid Maven command at index " + i + " -> " + e.getMessage());
			}

			mavenCommands.add(mavenCommand);
		}
		
		return mavenCommands;
	}
	
	public static MavenCommand mapAndValidateMavenCommand(MavenCommandProperty mavenCommandProperty) {
		
		ValidationUtils.notNull(mavenCommandProperty, "No Maven command defined");
		
		String goalsProperty = mavenCommandProperty.getGoals();
		Map<String, String> argumentsProperties = mavenCommandProperty.getArguments();
		Boolean printOutput = mavenCommandProperty.getPrintMavenOutput();
		
		ValueDefinition goals;
		try {
			
			goals = VariablesMapperValidator.mapAndValidateValueDefinition(goalsProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid Maven command goals -> " + e.getMessage());
		}
		
		
		List<VariableDefinition> arguments = null;
		if(!CollectionUtils.isEmpty(argumentsProperties)) {
			
			try {
				
				arguments = VariablesMapperValidator.mapAndValidateVariableDefinitions(argumentsProperties);
			}
			catch(Exception e) {
				
				throw new ValidationException("Maven command has an invalid list of arguments -> " + e.getMessage());
			}
		}
		
		MavenCommand mavenCommand = new MavenCommand();
		mavenCommand.setGoals(goals);
		mavenCommand.setArguments(arguments);
		mavenCommand.setPrintMavenOutput(printOutput != null && printOutput);
		return mavenCommand;
	}
}
