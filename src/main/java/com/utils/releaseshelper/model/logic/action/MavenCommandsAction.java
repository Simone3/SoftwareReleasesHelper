package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.ValueDefinition;
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

	private ValueDefinition projectFolder;
	private List<MavenCommand> commands;
	private GitCommit gitCommit;

	@Override
	public String getTypeDescription() {
		
		return "Maven Commands";
	}

	@Override
	public boolean isGitAction() {
		
		return gitCommit != null;
	}

	@Override
	public boolean isJenkinsAction() {
		
		return false;
	}

	@Override
	public boolean isMavenAction() {
		
		return true;
	}

	@Override
	public boolean isOperatingSystemAction() {
		
		return false;
	}
}
