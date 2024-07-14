package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for an action
 * It contains all fields of all types of actions because Spring properties do not support polymorphism
 */
@Data
public class ActionProperty {
	
	// Common
	private String name;
	private ActionTypeProperty type;
	private List<VariableDefinitionProperty> variables;
	
	// Type-specific
	private JenkinsBuildDefinitionProperty jenkinsBuildDefinition;
	private GitMergesDefinitionProperty gitMergesDefinition;
	private GitPullAllDefinitionProperty gitPullAllDefinition;
	private OperatingSystemCommandsDefinitionProperty osCommandsDefinition;
}
