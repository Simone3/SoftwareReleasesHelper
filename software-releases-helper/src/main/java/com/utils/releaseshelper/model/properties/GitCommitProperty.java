package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for a Git commit
 */
@Data
public class GitCommitProperty {

	private String branch;
	private Boolean pull;
	private String commitMessage;
}
