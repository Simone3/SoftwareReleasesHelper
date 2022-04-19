package com.utils.releaseshelper.model.service.git;

import lombok.Data;

/**
 * Service input for the Git commit all changes operation
 */
@Data
public class GitCommitChangesServiceInput {

	private String repositoryFolder;
	private String originalBranch;
	private String message;
}
