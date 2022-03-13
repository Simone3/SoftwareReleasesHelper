package com.utils.model.main;

import java.util.List;

import com.utils.model.git.GitOperation;
import com.utils.model.jenkins.JenkinsParameter;

import lombok.Data;

@Data
public class Action {
	
	private ActionType actionType;
	
	// Git action properties
	private String gitRepositoryFolder;
	private List<GitOperation> gitOperations;
	
	// Jenkins action properties
	private String buildUrl;
	private List<JenkinsParameter> buildParameters;
}
