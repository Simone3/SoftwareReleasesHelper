package com.utils.releaseshelper.model.logic.action;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;

/**
 * A generic action
 */
@Data
public abstract class Action {
	
	private String name;
	private boolean skipConfirmation;
	private ValueDefinition customDescription;
	
	public abstract String getTypeDescription();

	public abstract boolean isGitAction();

	public abstract boolean isJenkinsAction();

	public abstract boolean isMavenAction();

	public abstract boolean isOperatingSystemAction();
}
