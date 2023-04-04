package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.logic.action.WaitAction;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;
import com.utils.releaseshelper.model.logic.process.OperatingSystemCommand;
import com.utils.releaseshelper.model.properties.ActionProperty;
import com.utils.releaseshelper.model.properties.ActionTypeProperty;
import com.utils.releaseshelper.model.properties.GenericCommandProperty;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.model.properties.MavenProperties;
import com.utils.releaseshelper.utils.FileUtils;
import com.utils.releaseshelper.utils.UrlUtils;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates action properties
 */
@UtilityClass
public class ActionMapperValidator {

	public static Map<String, Action> mapAndValidateActions(List<ActionProperty> actionProperties, GitProperties gitProperties, JenkinsProperties jenkinsProperties, MavenProperties mavenProperties) {
		
		ValidationUtils.notEmpty(actionProperties, "At least one action should be defined");
		
		Map<String, Action> actions = new HashMap<>();
		
		for(int i = 0; i < actionProperties.size(); i++) {
			
			ActionProperty actionProperty = actionProperties.get(i);
			
			Action action;
			try {
				
				action = mapAndValidateAction(actionProperty, gitProperties, jenkinsProperties, mavenProperties);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid action at index " + i + " -> " + e.getMessage(), e);
			}

			String actionName = action.getName();
			if(actions.containsKey(actionName)) {
				
				throw new ValidationException("Action at index " + i + " has the same name of a previous action");
			}
			
			actions.put(actionName, action);
		}
		
		return actions;
	}

	public static Action mapAndValidateAction(ActionProperty actionProperty, GitProperties gitProperties, JenkinsProperties jenkinsProperties, MavenProperties mavenProperties) {
		
		ValidationUtils.notNull(actionProperty, "Action is empty");
		
		ActionTypeProperty type = ValidationUtils.notNull(actionProperty.getType(), "Action does not have a type");

		switch(type) {
		
			case DEFINE_VARIABLES:
				return mapAndValidateDefineVariablesAction(actionProperty);
				
			case MAVEN_COMMANDS:
				return mapAndValidateMavenCommandsAction(actionProperty, mavenProperties);
				
			case OPERATING_SYSTEM_COMMANDS:
				return mapAndValidateOperatingSystemCommandsAction(actionProperty);
			
			case GIT_MERGES:
				return mapAndValidateGitMergesAction(actionProperty, gitProperties);
				
			case JENKINS_BUILD:
				return mapAndValidateJenkinsBuildAction(actionProperty, jenkinsProperties);
				
			case WAIT:
				return mapAndValidateWaitAction(actionProperty);
				
			default:
				throw new ValidationException("Action has an unknown type: " + type);
		}
	}

	public static List<Action> mapAndValidateProjectActions(List<String> actionNames, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notEmpty(actionNames, "At least one action should be defined");
		
		List<Action> actions = new ArrayList<>();
		
		for(int i = 0; i < actionNames.size(); i++) {
			
			String actionName = actionNames.get(i);
			
			Action action;
			try {
				
				action = mapAndValidateProjectAction(actionName, actionDefinitions);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid action at index " + i + " -> " + e.getMessage(), e);
			}
			
			actions.add(action);
		}
		
		return actions;
	}

	public static Action mapAndValidateProjectAction(String actionName, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notBlank(actionName, "No action name defined");
		
		Action action = actionDefinitions.get(actionName);
		
		ValidationUtils.notNull(action, "No action defined for name \"" + actionName + "\"");
		
		return action;
	}

	private static void mapAndValidateGenericAction(ActionProperty actionProperty, Action action) {
		
		String name = ValidationUtils.notBlank(actionProperty.getName(), "Action does not have a name");
		Boolean skipConfirmation = actionProperty.getSkipConfirmation();
		String customDescriptionProperty = actionProperty.getCustomDescription();
		
		action.setName(name);
		action.setSkipConfirmation(skipConfirmation != null && skipConfirmation);
		action.setCustomDescription(customDescriptionProperty);
	}
	
	private static DefineVariablesAction mapAndValidateDefineVariablesAction(ActionProperty actionProperty) {
		
		Map<String, String> variableProperties = actionProperty.getVariables();
		
		List<VariableDefinition> variables;
		try {
			
			variables = VariablesMapperValidator.mapAndValidateVariableDefinitions(variableProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Define variables action has an invalid list of variables -> " + e.getMessage(), e);
		}

		DefineVariablesAction action = new DefineVariablesAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setVariables(variables);
		return action;
	}

	private static MavenCommandsAction mapAndValidateMavenCommandsAction(ActionProperty actionProperty, MavenProperties mavenProperties) {
		
		String projectFolderProperty = ValidationUtils.notBlank(actionProperty.getProjectFolder(), "Maven commands action does not have a project folder");
		List<GenericCommandProperty> commandProperties = actionProperty.getCommands();
		GitCommitProperty gitCommitProperty = actionProperty.getGitCommit();
		
		List<MavenCommand> commands = null;
		try {
			
			commands = MavenMapperValidator.mapAndValidateMavenCommands(commandProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Maven commands action has an invalid list of commands -> " + e.getMessage(), e);
		}
		
		GitCommit gitCommit = null;
		if(gitCommitProperty != null) {
			
			try {
				
				gitCommit = GitMapperValidator.mapAndValidateGitCommit(gitCommitProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Maven commands action has an invalid Git commit definition -> " + e.getMessage(), e);
			}
		}
		
		String fullProjectFolder = FileUtils.getFullPath(mavenProperties == null ? null : mavenProperties.getBasePath(), projectFolderProperty);
		
		ValueDefinition projectFolder;
		try {
			
			projectFolder = VariablesMapperValidator.mapAndValidateValueDefinition(fullProjectFolder);
		}
		catch(Exception e) {
			
			throw new ValidationException("Maven commands action has an invalid project folder -> " + e.getMessage(), e);
		}
		
		MavenCommandsAction action = new MavenCommandsAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setProjectFolder(projectFolder);
		action.setCommands(commands);
		action.setGitCommit(gitCommit);
		return action;
	}

	private static OperatingSystemCommandsAction mapAndValidateOperatingSystemCommandsAction(ActionProperty actionProperty) {
		
		String folderProperty = ValidationUtils.notBlank(actionProperty.getFolder(), "Operating system commands action does not have a folder");
		List<GenericCommandProperty> commandProperties = actionProperty.getCommands();
		GitCommitProperty gitCommitProperty = actionProperty.getGitCommit();
		
		List<OperatingSystemCommand> commands = null;
		try {
			
			commands = OperatingSystemMapperValidator.mapAndValidateOperatingSystemCommands(commandProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Operating system commands action has an invalid list of commands -> " + e.getMessage(), e);
		}
		
		GitCommit gitCommit = null;
		if(gitCommitProperty != null) {
			
			try {
				
				gitCommit = GitMapperValidator.mapAndValidateGitCommit(gitCommitProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Operating system commands action has an invalid Git commit definition -> " + e.getMessage(), e);
			}
		}
		
		ValueDefinition folder;
		try {
			
			folder = VariablesMapperValidator.mapAndValidateValueDefinition(folderProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Operating system commands action has an invalid folder -> " + e.getMessage(), e);
		}
		
		OperatingSystemCommandsAction action = new OperatingSystemCommandsAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setFolder(folder);
		action.setCommands(commands);
		action.setGitCommit(gitCommit);
		return action;
	}

	private static GitMergesAction mapAndValidateGitMergesAction(ActionProperty actionProperty, GitProperties gitProperties) {
		
		String repositoryFolderProperty = ValidationUtils.notBlank(actionProperty.getRepositoryFolder(), "Git merges action does not have a repository folder");
		String mergesProperties = ValidationUtils.notBlank(actionProperty.getMerges(), "Git merges action does not have a merge definition");
		Boolean pullProperty = actionProperty.getPull();
		
		ValueDefinition merges;
		try {
			
			merges = VariablesMapperValidator.mapAndValidateValueDefinition(mergesProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Git merges action has an invalid list of merges -> " + e.getMessage(), e);
		}
		
		String fullRepositoryFolder = FileUtils.getFullPath(gitProperties == null ? null : gitProperties.getBasePath(), repositoryFolderProperty);
		
		ValueDefinition repositoryFolder;
		try {
			
			repositoryFolder = VariablesMapperValidator.mapAndValidateValueDefinition(fullRepositoryFolder);
		}
		catch(Exception e) {
			
			throw new ValidationException("Git merges action has an invalid repository folder -> " + e.getMessage(), e);
		}

		GitMergesAction action = new GitMergesAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setRepositoryFolder(repositoryFolder);
		action.setMerges(merges);
		action.setPull(pullProperty != null && pullProperty);
		return action;
	}

	private static JenkinsBuildAction mapAndValidateJenkinsBuildAction(ActionProperty actionProperty, JenkinsProperties jenkinsProperties) {
		
		String urlProperty = ValidationUtils.notBlank(actionProperty.getUrl(), "Jenkins build action does not have a URL");
		Map<String, String> parametersProperties = actionProperty.getParameters();
		
		List<VariableDefinition> parameters = null;
		if(!CollectionUtils.isEmpty(parametersProperties)) {
			
			try {
				
				parameters = VariablesMapperValidator.mapAndValidateVariableDefinitions(parametersProperties);
			}
			catch(Exception e) {
				
				throw new ValidationException("Jenkins build action has an invalid list of parameters -> " + e.getMessage(), e);
			}
		}
		
		String fullUrl = UrlUtils.getFullUrl(jenkinsProperties.getBaseUrl(), urlProperty);
		
		ValueDefinition url;
		try {
			
			url = VariablesMapperValidator.mapAndValidateValueDefinition(fullUrl);
		}
		catch(Exception e) {
			
			throw new ValidationException("Jenkins build action has an invalid project folder -> " + e.getMessage(), e);
		}
		
		JenkinsBuildAction action = new JenkinsBuildAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setUrl(url);
		action.setParameters(parameters);
		return action;
	}

	private static WaitAction mapAndValidateWaitAction(ActionProperty actionProperty) {
		
		Integer waitTimeMilliseconds = actionProperty.getWaitTimeMilliseconds();
		String manualWaitPrompt = actionProperty.getManualWaitPrompt();
		
		ValidationUtils.isTrue(waitTimeMilliseconds != null || !StringUtils.isBlank(manualWaitPrompt), "Wait action must have a wait time and/or a manual wait prompt");
		
		if(waitTimeMilliseconds != null) {
			
			ValidationUtils.positive(waitTimeMilliseconds, "Wait action has an invalid wait time");
		}
	
		WaitAction action = new WaitAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setWaitTimeMilliseconds(waitTimeMilliseconds);
		action.setManualWaitPrompt(manualWaitPrompt);
		return action;
	}
}
