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
	public boolean requiresGitConfig() {
		
		return false;
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
