package com.utils.releaseshelper.model.logic;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * The state for a Git Merges Action
 */
@Data
public class GitMergesActionState implements ActionState {
	
	private static final long serialVersionUID = 1L;
	
	private GitMergesActionSuspensionStep suspensionStep;
	private String folder;
	private String mergesString;
	private List<GitMerge> merges;
	private boolean pull;
	private int currentMergeIndex;
	private String originalBranch;
	private Set<String> pulledBranches;
	
	public void incrementCurrentMergeIndex() {
		
		currentMergeIndex += 1;
	}
}
