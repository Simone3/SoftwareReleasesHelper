package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.VariableDefinition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to define one or more variables
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DefineVariablesAction extends Action {

	private List<VariableDefinition> variables;

	@Override
	public String getTypeDescription() {
		
		return "Define Variables";
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
