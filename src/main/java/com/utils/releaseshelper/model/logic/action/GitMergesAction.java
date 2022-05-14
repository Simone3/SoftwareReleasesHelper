package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.git.GitMerge;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run one or more Git merges
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitMergesAction extends Action {

	private String repositoryFolder;
	private List<GitMerge> merges;

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
