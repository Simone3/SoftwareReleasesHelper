package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for a Git merge
 */
@Data
public class GitMergeProperty {

	private Boolean pull;
	private String sourceBranch;
	private String targetBranch;
}
