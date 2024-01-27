package com.utils.releaseshelper.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run one or more Git merges
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitMergesAction extends Action {

	private String repositoryFolder;
	private String merges;
	private boolean pull;
	
	public GitMergesAction() {
		
		super(ActionType.GIT_MERGES);
	}
	
	@Override
	public String getTypeDescription() {
		
		return "Git Merges";
	}
}
