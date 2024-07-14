package com.utils.releaseshelper.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run Git pulls on all repositories of a folder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitPullAllAction extends Action {

	private String parentFolder;
	private boolean skipIfWorkingTreeDirty;
	
	public GitPullAllAction() {
		
		super(ActionType.GIT_PULL_ALL);
	}
	
	@Override
	public String getTypeDescription() {
		
		return "Git Pull All";
	}
}
