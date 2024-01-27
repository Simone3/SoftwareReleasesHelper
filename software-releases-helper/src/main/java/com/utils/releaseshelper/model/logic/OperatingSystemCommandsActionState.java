package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.domain.GitCommit;
import com.utils.releaseshelper.model.domain.OperatingSystemCommand;

import lombok.Data;

/**
 * The state for an Operating System Commands Action
 */
@Data
public class OperatingSystemCommandsActionState implements ActionState {
	
	private static final long serialVersionUID = 1L;
	
	private String folder;
	private List<OperatingSystemCommand> commands;
	private int currentCommandIndex;
	private GitCommit gitCommit;
	private String originalGitBranch;
	private boolean actuallyGitCommitted;
}
