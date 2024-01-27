package com.utils.releaseshelper.model.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * All application properties, parsed by Spring
 */
@Data
@ConfigurationProperties(ignoreInvalidFields = true, ignoreUnknownFields = true)
public class Properties {
	
	private Boolean webGui;
	private Boolean testMode;
	private Boolean printPasswords;
	private JenkinsProperties jenkins;
	private GitProperties git;
	private List<ActionProperty> actionDefinitions;
}
