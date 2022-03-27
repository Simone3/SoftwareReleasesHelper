package com.utils.logic.jenkins;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import com.utils.logic.common.StepLogic;
import com.utils.model.jenkins.JenkinsData;
import com.utils.model.jenkins.JenkinsStep;
import com.utils.model.main.Action;
import com.utils.model.properties.Properties;
import com.utils.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JenkinsLogic extends StepLogic<JenkinsStep>{

	private final CommandLineInterface cli;
	private final JenkinsService service;

	private final boolean printPassword;
	private final JenkinsData jenkinsData;
	
	private String crumb;
	private Action build;
	private Map<String, String> parameterValues;

	public JenkinsLogic(Properties properties, CommandLineInterface cli) {
		
		super(JenkinsStep.DEFINE_PARAMETERS, JenkinsStep.EXIT);
		this.cli = cli;
		this.service = properties.isTestMode() ? new JenkinsServiceMock() : new JenkinsServiceReal(properties.getJenkins());
		this.jenkinsData = properties.getJenkins();
		this.printPassword = properties.isPrintPasswords();
	}
	
	public void execute(Action action) {
		
		build = action;
		loopSteps();
	}

	@Override
	protected JenkinsStep processCurrentStep(JenkinsStep currentStep) {

		switch(currentStep) {
				
			case DEFINE_PARAMETERS:
				return defineParameters();
			
			case START_BUILD:
				return startBuild();
				
			default:
				throw new IllegalStateException("Unknown step: " + currentStep);
		}
	}
	
	private JenkinsStep defineParameters() {
		
		parameterValues = new LinkedHashMap<>();
		
		boolean manuallyDefined = false;
		
		for(var parameter: build.getBuildParameters()) {
			
			String key = parameter.getKey();
			
			String value;
			if(parameter.isAskMe()) {
				
				if(!manuallyDefined) {
					
					cli.println("Manually define these build parameters:");
					manuallyDefined = true;
				}
				
				String whitespaceNote = parameter.isRemoveWhitespace() ? " (all spaces will be removed)" : "";
				value = cli.getUserInput("  - %s%s: ", key, whitespaceNote);
			}
			else {
				
				value = parameter.getValue();
			}
			
			if(parameter.isRemoveWhitespace()) {
				
				value = value.replaceAll("\\s+", "");
				value = value.replace("\u200B", "");
			}

			parameterValues.put(key, value);
		}
		
		if(manuallyDefined) {
			
			cli.printSeparator();
		}
		
		return JenkinsStep.START_BUILD;
	}
	
	private JenkinsStep startBuild() {
		
		String crumbUrl = URI.create(jenkinsData.getBaseUrl()).resolve(jenkinsData.getCrumbUrl()).toString();
		String buildUrl = URI.create(jenkinsData.getBaseUrl()).resolve(build.getBuildUrl()).toString();
		String username = jenkinsData.getUsername();
		String password = jenkinsData.getPassword();
		Map<String, String> parameters = parameterValues;
		
		cli.println("Start build with:");
		cli.println("  - Crumb URL: %s", crumbUrl);
		cli.println("  - Build URL: %s", buildUrl);
		cli.println("  - Username: %s", username);
		cli.println("  - Password: %s", (printPassword ? password : "******"));
		cli.println("  - Parameters:");
		for(var paramEntry: parameters.entrySet()) {
			
			cli.println("     - %s: %s", paramEntry.getKey(), paramEntry.getValue());
		}

		cli.println();
		if(cli.askUserConfirmation("Start build")) {

			cli.printSeparator();
			startBuildWithRetries(crumbUrl, buildUrl, username, password, parameters);
		}
		else {
			
			cli.println();
			cli.printSeparator(false);
		}
		
		clearState();
		return JenkinsStep.EXIT;
	}

	private boolean startBuildWithRetries(String crumbUrl, String buildUrl, String username, String password, Map<String, String> parameters) {
		
		boolean first = true;
		
		while(first || cli.askUserConfirmation("Retry build")) {
			
			if(first) {
				
				first = false;
			}
			else {
				
				cli.printSeparator();
			}
			
			try {
				
				if(crumb == null) {
					
					cli.println("Getting Jenkins crumb...");
					crumb = service.getCrumb(crumbUrl, username, password);
					cli.println("Got Jenkins crumb: %s", crumb);
				}
				
				cli.println("Starting build...");
				service.startBuild(buildUrl, username, password, crumb, parameters);
				cli.println("Build started successfully!");
				
				return true;
			}
			catch(Exception e) {
				
				cli.printError("Cannot start build: %s", e.getMessage());
				log.error("Error starting build", e);
			}
		}
		
		cli.println();
		cli.printSeparator(false);
		
		return false;
	}

	private void clearState() {
		
		this.build = null;
		this.parameterValues = null;
	}
}
