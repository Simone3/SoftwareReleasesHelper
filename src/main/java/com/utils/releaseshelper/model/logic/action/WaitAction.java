package com.utils.releaseshelper.model.logic.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to wait for a specified amount of time and/or user input
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WaitAction extends Action {

	private Integer waitTimeMilliseconds;
	private String manualWaitPrompt;

	@Override
	public String getTypeDescription() {
		
		return "Wait";
	}

	@Override
	public boolean isGitAction() {
		
		return false;
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
