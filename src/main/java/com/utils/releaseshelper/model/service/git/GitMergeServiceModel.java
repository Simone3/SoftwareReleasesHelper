package com.utils.releaseshelper.model.service.git;

import lombok.Data;

/**
 * Description of a Git merge
 */
@Data
public class GitMergeServiceModel {

	private String sourceBranch;
	private String targetBranch;
	private boolean pull;
}
