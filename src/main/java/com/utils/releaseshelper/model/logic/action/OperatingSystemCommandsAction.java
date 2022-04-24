package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.process.OperatingSystemCommand;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to run one or more generic operating system commands, optionally committing all changes (if any) to a Git repository
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperatingSystemCommandsAction extends Action {

	private String folder;
	private List<OperatingSystemCommand> commands;
	private GitCommit gitCommit;

	@Override
	public String getTypeDescription() {
		
		return "Operating System Commands";
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
		
		return false;
	}
}
