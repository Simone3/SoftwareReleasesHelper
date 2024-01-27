package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for Git Merges Action data
 */
@Data
public class GitMergesDefinitionProperty {
	
	private String repositoryFolder;
	private String merges;
	private Boolean pull;
}