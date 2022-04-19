package com.utils.releaseshelper.logic.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.service.jenkins.JenkinsBuildServiceInput;
import com.utils.releaseshelper.service.jenkins.JenkinsService;
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to start a Jenkins build
 */
public class JenkinsBuildActionLogic extends ActionLogic<JenkinsBuildAction> {
	
	private final JenkinsService jenkinsService;
	
	private Map<String, String> buildParams;

	protected JenkinsBuildActionLogic(JenkinsBuildAction action, Map<String, String> variables, CommandLineInterface cli, JenkinsService jenkinsService) {
		
		super(action, variables, cli);
		this.jenkinsService = jenkinsService;
	}

	@Override
	protected void beforeAction() {
		
		buildParams = new LinkedHashMap<>();
		
		List<VariableDefinition> parameters = action.getParameters();
		for(VariableDefinition parameter: parameters) {
			
			String value = VariablesUtils.defineVariable(cli, "Define Jenkins build parameter", parameter, variables);
			buildParams.put(parameter.getKey(), value);
		}
	}

	@Override
	protected void printActionDescription() {
		
		String buildUrl = action.getUrl();
		
		if(buildParams.isEmpty()) {
			
			cli.println("Jenkins build %s without parameters", buildUrl);
		}
		else {
		
			cli.println("Jenkins build %s with parameters:", buildUrl);
			for(Entry<String, String> paramEntry: buildParams.entrySet()) {
				
				cli.println("  - %s: %s", paramEntry.getKey(), paramEntry.getValue());
			}
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Start build";
	}

	@Override
	protected void doRunAction() {
		
		JenkinsBuildServiceInput buildInput = mapBuildServiceInput();
		jenkinsService.startBuild(buildInput);
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}
	
	private JenkinsBuildServiceInput mapBuildServiceInput() {
		
		JenkinsBuildServiceInput input = new JenkinsBuildServiceInput();
		input.setUrl(action.getUrl());
		input.setParameters(buildParams);
		return input;
	}
}
