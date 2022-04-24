package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;

/**
 * A Git merge
 */
@Data
public class GitMerge {

	private ValueDefinition sourceBranch;
	private ValueDefinition targetBranch;
	private boolean pull;
}
