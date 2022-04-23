package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;

/**
 * Description of a Git merge
 */
@Data
public class GitMerge {

	private ValueDefinition sourceBranch;
	private ValueDefinition targetBranch;
	private boolean pull;
}
