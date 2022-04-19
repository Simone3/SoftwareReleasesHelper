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
	public boolean requiresGitConfig() {
		
		return true;
	}

	@Override
	public boolean requiresJenkinsConfig() {
		
		return false;
	}

	@Override
	public boolean requiresMavenConfig() {
		
		return false;
	}
}
