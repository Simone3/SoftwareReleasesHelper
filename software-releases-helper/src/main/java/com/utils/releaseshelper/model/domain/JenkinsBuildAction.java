package com.utils.releaseshelper.model.domain;

import java.util.List;

import com.utils.releaseshelper.model.misc.KeyValuePair;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action to start a Jenkins build
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JenkinsBuildAction extends Action {

	private String url;
	private List<KeyValuePair> parameters;
	
	public JenkinsBuildAction() {
		
		super(ActionType.JENKINS_BUILD);
	}
	
	@Override
	public String getTypeDescription() {
		
		return "Jenkins Build";
	}
}
