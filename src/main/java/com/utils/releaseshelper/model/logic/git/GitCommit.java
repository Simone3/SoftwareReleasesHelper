package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;

/**
 * Description of a Git commit
 */
@Data
public class GitCommit {

	private ValueDefinition branch;
	private boolean pull;
	private ValueDefinition message;
}
