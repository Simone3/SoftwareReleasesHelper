package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for Git Pull All Action data
 */
@Data
public class GitPullAllDefinitionProperty {
	
	private String parentFolder;
	private Boolean skipIfWorkingTreeDirty;
}