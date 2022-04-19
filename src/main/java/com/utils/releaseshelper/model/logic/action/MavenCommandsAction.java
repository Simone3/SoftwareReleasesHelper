package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run one or more generic Maven commands, optionally committing all changes (if any) to a Git repository (e.g. for "versions:set" plugin)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MavenCommandsAction extends Action {

	private String projectFolder;
	private List<MavenCommand> commands;
	private GitCommit gitCommit;

	@Override
	public String getTypeDescription() {
		
		return "Maven Commands";
	}

	@Override
	public boolean requiresGitConfig() {
		
		return gitCommit != null;
	}

	@Override
	public boolean requiresJenkinsConfig() {
		
		return false;
	}

	@Override
	public boolean requiresMavenConfig() {
		
		return true;
	}
}
