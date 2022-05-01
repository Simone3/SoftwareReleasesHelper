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

	public abstract boolean requiresGitConfig();

	public abstract boolean requiresJenkinsConfig();

	public abstract boolean requiresMavenConfig();
}
