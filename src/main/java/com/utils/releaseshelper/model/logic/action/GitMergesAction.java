package com.utils.releaseshelper.model.logic.action;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run one or more Git merges
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitMergesAction extends Action {

	private ValueDefinition repositoryFolder;
	private ValueDefinition merges;
	private boolean pull;

	@Override
	public String getTypeDescription() {
		
		return "Git Merges";
	}

	@Override
	public boolean isGitAction() {
		
		return true;
	}

	@Override
	public boolean isJenkinsAction() {
		
		return false;
	}

	@Override
	public boolean isMavenAction() {
		
		return false;
	}

	@Override
	public boolean isOperatingSystemAction() {
		
		return false;
	}
}
