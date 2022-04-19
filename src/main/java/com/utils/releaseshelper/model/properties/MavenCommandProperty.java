package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for a Maven command
 */
@Data
public class MavenCommandProperty {
	
	private String goals;
	private List<VariableDefinitionProperty> arguments;
	private Boolean printMavenOutput;
}
