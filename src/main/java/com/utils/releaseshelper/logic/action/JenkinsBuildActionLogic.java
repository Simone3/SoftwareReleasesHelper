package com.utils.releaseshelper.logic.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.service.jenkins.JenkinsBuildServiceInput;
import com.utils.releaseshelper.service.jenkins.JenkinsService;
import com.utils.releaseshelper.utils.ValuesDefiner;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to start a Jenkins build
 */
public class JenkinsBuildActionLogic extends ActionLogic<JenkinsBuildAction> {
	
	private final JenkinsService jenkinsService;

	protected JenkinsBuildActionLogic(JenkinsBuildAction action, Map<String, String> variables, CommandLineInterface cli, JenkinsService jenkinsService) {
		
		super(action, variables, cli);
		this.jenkinsService = jenkinsService;
	}

	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {
		
		List<VariableDefinition> parameters = action.getParameters();
		for(VariableDefinition parameter: parameters) {
			
			valuesDefiner.addValueDefinition(parameter.getValueDefinition(), parameter.getKey() + " argument");
		}
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		String buildUrl = action.getUrl();
		
		List<VariableDefinition> parameters = action.getParameters();
		if(parameters.isEmpty()) {
			
			cli.println("Jenkins build %s without parameters", buildUrl);
		}
		else {
		
			cli.println("Jenkins build %s with parameters:", buildUrl);
			for(VariableDefinition parameter: parameters) {
				
				cli.println("  - %s: %s", parameter.getKey(), valuesDefiner.getValue(parameter.getValueDefinition()));
			}
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Start build";
	}

	@Override
	protected void doRunAction(ValuesDefiner valuesDefiner) {
		
		JenkinsBuildServiceInput buildInput = mapBuildServiceInput(valuesDefiner);
		jenkinsService.startBuild(buildInput);
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}
	
	private JenkinsBuildServiceInput mapBuildServiceInput(ValuesDefiner valuesDefiner) {
		
		Map<String, String> buildParams = new HashMap<>();
		for(VariableDefinition parameter: action.getParameters()) {
			
			buildParams.put(parameter.getKey(), valuesDefiner.getValue(parameter.getValueDefinition()));
		}
		
		JenkinsBuildServiceInput input = new JenkinsBuildServiceInput();
		input.setUrl(action.getUrl());
		input.setParameters(buildParams);
		return input;
	}
}
