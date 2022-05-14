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
