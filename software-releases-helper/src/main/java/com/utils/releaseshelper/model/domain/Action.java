package com.utils.releaseshelper.model.domain;

import java.util.List;

import lombok.Data;

/**
 * A generic action
 */
@Data
public abstract class Action {
	
	private final ActionType type;
	private String name;
	private List<VariableDefinition> variables;
	
	public abstract String getTypeDescription();
}
