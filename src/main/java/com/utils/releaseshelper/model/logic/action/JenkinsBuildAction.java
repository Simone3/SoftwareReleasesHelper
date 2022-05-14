package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import com.utils.releaseshelper.model.logic.VariableDefinition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to start a Jenkins build
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JenkinsBuildAction extends Action {

	private String url;
	private List<VariableDefinition> parameters;

	@Override
	public String getTypeDescription() {
		
		return "Jenkins Build";
	}

	@Override
	public boolean isGitAction() {
		
		return false;
	}

	@Override
	public boolean isJenkinsAction() {
		
		return true;
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
