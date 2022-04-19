package com.utils.releaseshelper.model.logic.git;

import lombok.Data;

/**
 * Description of a Git commit
 */
@Data
public class GitCommit {

	private String branch;
	private boolean pull;
	private String message;
}
