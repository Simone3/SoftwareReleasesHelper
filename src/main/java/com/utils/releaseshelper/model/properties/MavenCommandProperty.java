package com.utils.releaseshelper.model.properties;

import java.util.Map;

import lombok.Data;

/**
 * A property for a Maven command
 */
@Data
public class MavenCommandProperty {
	
	private String goals;
	private Map<String, String> arguments;
	private Boolean offline;
	private Boolean printMavenOutput;
}
