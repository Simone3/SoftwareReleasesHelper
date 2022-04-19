package com.utils.releaseshelper.model.service.git;

import lombok.Data;

/**
 * Service input for the prepare Git changes operation
 */
@Data
public class GitPrepareForChangesServiceInput {

	private String repositoryFolder;
	private boolean pull;
	private String branch;
}
