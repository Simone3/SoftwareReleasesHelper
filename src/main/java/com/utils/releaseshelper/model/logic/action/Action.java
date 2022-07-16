package com.utils.releaseshelper.model.logic.action;

import lombok.Data;

/**
 * A generic action
 */
@Data
public abstract class Action {
	
	private String name;
	private boolean skipConfirmation;
	private String customDescription;
	
	public abstract String getTypeDescription();

	public abstract boolean isGitAction();

	public abstract boolean isJenkinsAction();

	public abstract boolean isMavenAction();

	public abstract boolean isOperatingSystemAction();
}
