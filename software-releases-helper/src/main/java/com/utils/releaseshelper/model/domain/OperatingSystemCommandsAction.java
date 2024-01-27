package com.utils.releaseshelper.model.domain;

import java.util.List;

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
	
	public OperatingSystemCommandsAction() {
		
		super(ActionType.OPERATING_SYSTEM_COMMANDS);
	}
	
	@Override
	public String getTypeDescription() {
		
		return "Operating System Commands";
	}
}
