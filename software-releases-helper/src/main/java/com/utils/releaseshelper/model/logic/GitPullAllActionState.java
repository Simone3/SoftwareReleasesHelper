package com.utils.releaseshelper.model.logic;

import lombok.Data;

/**
 * The state for a Git Pull All Action
 */
@Data
public class GitPullAllActionState implements ActionState {
	
	private static final long serialVersionUID = 1L;
	
	private String parentFolder;
	private boolean skipIfWorkingTreeDirty;
	private int pulledRepos;
	private int dirtyRepos;
	private int errorRepos;
	
	public void incrementPulledRepos() {
		
		pulledRepos += 1;
	}
	
	public void incrementDirtyRepos() {
		
		dirtyRepos += 1;
	}
	
	public void incrementErrorRepos() {
		
		errorRepos += 1;
	}
}
