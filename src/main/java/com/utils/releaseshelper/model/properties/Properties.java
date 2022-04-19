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
	
	private Boolean testMode;
	private Boolean printPasswords;
	private GitProperties git;
	private JenkinsProperties jenkins;
	private MavenProperties maven;
	private List<ActionProperty> actionDefinitions;
	private List<CategoryProperty> categories;
	private String optionalPreSelectedCategoryIndex;
	private String optionalPreSelectedProjectIndices;
}
