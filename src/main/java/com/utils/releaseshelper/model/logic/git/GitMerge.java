package com.utils.releaseshelper.model.logic.git;

import lombok.Data;

/**
 * Description of a Git merge
 */
@Data
public class GitMerge {

	private String sourceBranch;
	private String targetBranch;
	private boolean pull;
}
