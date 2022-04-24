package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.ChainAction;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.git.GitMerge;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;
import com.utils.releaseshelper.model.logic.process.OperatingSystemCommand;
import com.utils.releaseshelper.model.properties.ActionProperty;
import com.utils.releaseshelper.model.properties.ActionTypeProperty;
import com.utils.releaseshelper.model.properties.GenericCommandProperty;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitMergeProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.model.properties.MavenProperties;
import com.utils.releaseshelper.utils.FileUtils;
import com.utils.releaseshelper.utils.UrlUtils;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Maps and validates action properties
 */
@UtilityClass
public class ActionMapperValidator {

	public static Map<String, Action> mapAndValidateActions(List<ActionProperty> actionProperties, GitProperties gitProperties, JenkinsProperties jenkinsProperties, MavenProperties mavenProperties) {
		
		ValidationUtils.notEmpty(actionProperties, "At least one action should be defined");
		
		Map<String, Action> actions = new HashMap<>();
		
		// Sort actions in order to put special chain actions at the end (while storing the original index for error messages)
		List<ActionPropertyContainer> actionPropertyContainers = sortActionProperties(actionProperties);
		
		for(ActionPropertyContainer container: actionPropertyContainers) {
			
			int i = container.originalIndex;
			ActionProperty actionProperty = container.actionProperty;
			
			Action action;
			try {
				
				if(actionProperty != null && actionProperty.getType() == ActionTypeProperty.CHAIN) {
					
					// Special handling for chain actions
					action = mapAndValidateChainAction(actionProperty, actions);
				}
				else {
					
					// Standard actions mapping
					action = mapAndValidateAction(actionProperty, gitProperties, jenkinsProperties, mavenProperties);
				}
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid action at index " + i + " -> " + e.getMessage());
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
				
			case CHAIN:
				throw new ValidationException("Chain action cannot be mapped as a single action, because of its dependency with other actions");
		
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
				
				throw new ValidationException("Invalid action at index " + i + " -> " + e.getMessage());
			}
			
			actions.add(action);
		}
		
		return actions;
	}

	public static Action mapAndValidateProjectAction(String actionName, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notBlank(actionName, "No action name defined");
		
		Action action = actionDefinitions.get(actionName);
		
		ValidationUtils.notNull(action, "No action defined for name " + actionName);
		
		return action;
	}
	
	private static List<ActionPropertyContainer> sortActionProperties(List<ActionProperty> actionProperties) {

		List<ActionPropertyContainer> chainActions = new ArrayList<>();
		List<ActionPropertyContainer> standardActions = new ArrayList<>();
		
		for(int i = 0; i < actionProperties.size(); i++) {
			
			ActionProperty actionProperty = actionProperties.get(i);
			
			if(actionProperty != null && actionProperty.getType() == ActionTypeProperty.CHAIN) {
				
				chainActions.add(new ActionPropertyContainer(i, actionProperty));
			}
			else {
				
				standardActions.add(new ActionPropertyContainer(i, actionProperty));
			}
		}
		
		standardActions.addAll(chainActions);
		
		return standardActions;
	}

	private static void mapAndValidateGenericAction(ActionProperty actionProperty, Action action) {
		
		String name = ValidationUtils.notBlank(actionProperty.getName(), "Action does not have a name");
		Boolean skipConfirmation = actionProperty.getSkipConfirmation();

		action.setName(name);
		action.setSkipConfirmation(skipConfirmation != null && skipConfirmation);
	}
	
	private static DefineVariablesAction mapAndValidateDefineVariablesAction(ActionProperty actionProperty) {
		
		Map<String, String> variableProperties = actionProperty.getVariables();
		
		List<VariableDefinition> variables;
		try {
			
			variables = VariablesMapperValidator.mapAndValidateVariableDefinitions(variableProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Define variables action has an invalid list of variables -> " + e.getMessage());
		}

		DefineVariablesAction action = new DefineVariablesAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setVariables(variables);
		return action;
	}

	private static MavenCommandsAction mapAndValidateMavenCommandsAction(ActionProperty actionProperty, MavenProperties mavenProperties) {
		
		String projectFolder = ValidationUtils.notBlank(actionProperty.getProjectFolder(), "Maven commands action does not have a project folder");
		List<GenericCommandProperty> commandProperties = actionProperty.getCommands();
		GitCommitProperty gitCommitProperty = actionProperty.getGitCommit();
		
		String fullProjectFolder = FileUtils.getFullPath(mavenProperties == null ? null : mavenProperties.getBasePath(), projectFolder);
		
		List<MavenCommand> commands = null;
		try {
			
			commands = MavenMapperValidator.mapAndValidateMavenCommands(commandProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Maven commands action has an invalid list of commands -> " + e.getMessage());
		}
		
		GitCommit gitCommit = null;
		if(gitCommitProperty != null) {
			
			try {
				
				gitCommit = GitMapperValidator.mapAndValidateGitCommit(gitCommitProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Maven commands action has an invalid Git commit definition -> " + e.getMessage());
			}
		}
		
		MavenCommandsAction action = new MavenCommandsAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setProjectFolder(fullProjectFolder);
		action.setCommands(commands);
		action.setGitCommit(gitCommit);
		return action;
	}

	private static OperatingSystemCommandsAction mapAndValidateOperatingSystemCommandsAction(ActionProperty actionProperty) {
		
		String folder = ValidationUtils.notBlank(actionProperty.getFolder(), "Operating system commands action does not have a folder");
		List<GenericCommandProperty> commandProperties = actionProperty.getCommands();
		GitCommitProperty gitCommitProperty = actionProperty.getGitCommit();
		
		List<OperatingSystemCommand> commands = null;
		try {
			
			commands = OperatingSystemMapperValidator.mapAndValidateOperatingSystemCommands(commandProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Operating system commands action has an invalid list of commands -> " + e.getMessage());
		}
		
		GitCommit gitCommit = null;
		if(gitCommitProperty != null) {
			
			try {
				
				gitCommit = GitMapperValidator.mapAndValidateGitCommit(gitCommitProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Operating system commands action has an invalid Git commit definition -> " + e.getMessage());
			}
		}
		
		OperatingSystemCommandsAction action = new OperatingSystemCommandsAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setFolder(folder);
		action.setCommands(commands);
		action.setGitCommit(gitCommit);
		return action;
	}

	private static GitMergesAction mapAndValidateGitMergesAction(ActionProperty actionProperty, GitProperties gitProperties) {
		
		String repositoryFolder = ValidationUtils.notBlank(actionProperty.getRepositoryFolder(), "Git merges action does not have a repository folder");
		List<GitMergeProperty> mergesProperties = actionProperty.getMerges();
		
		List<GitMerge> merges;
		try {
			
			merges = GitMapperValidator.mapAndValidateGitMerges(mergesProperties);
		}
		catch(Exception e) {
			
			throw new ValidationException("Git merges action has an invalid list of merges -> " + e.getMessage());
		}
		
		String fullRepositoryFolder = FileUtils.getFullPath(gitProperties == null ? null : gitProperties.getBasePath(), repositoryFolder);

		GitMergesAction action = new GitMergesAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setRepositoryFolder(fullRepositoryFolder);
		action.setMerges(merges);
		return action;
	}

	private static JenkinsBuildAction mapAndValidateJenkinsBuildAction(ActionProperty actionProperty, JenkinsProperties jenkinsProperties) {
		
		String url = ValidationUtils.notBlank(actionProperty.getUrl(), "Jenkins build action does not have a URL");
		Map<String, String> parametersProperties = actionProperty.getParameters();
		
		List<VariableDefinition> parameters = null;
		if(!CollectionUtils.isEmpty(parametersProperties)) {
			
			try {
				
				parameters = VariablesMapperValidator.mapAndValidateVariableDefinitions(parametersProperties);
			}
			catch(Exception e) {
				
				throw new ValidationException("Jenkins build action has an invalid list of parameters -> " + e.getMessage());
			}
		}
		
		String fullUrl = UrlUtils.getFullUrl(jenkinsProperties.getBaseUrl(), url);
		
		JenkinsBuildAction action = new JenkinsBuildAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setUrl(fullUrl);
		action.setParameters(parameters);
		return action;
	}
	
	private static ChainAction mapAndValidateChainAction(ActionProperty actionProperty, Map<String, Action> allActions) {

		List<String> actionIds = ValidationUtils.notEmpty(actionProperty.getActions(), "Chain action does not have a list of sub-actions");
		
		List<Action> actions = new ArrayList<>();
		for(String actionId: actionIds) {
			
			Action subAction = allActions.get(actionId);
			if(subAction == null) {
				
				throw new ValidationException("Sub-action " + actionId + " of the chain action does not exit");
			}
			if(subAction instanceof ChainAction) {
				
				throw new ValidationException("Sub-action " + actionId + " of the chain action cannot be a chain action itself");
			}
			actions.add(subAction);
		}
		
		ChainAction action = new ChainAction();
		mapAndValidateGenericAction(actionProperty, action);
		action.setActions(actions);
		return action;
	}
	
	@RequiredArgsConstructor
	private static class ActionPropertyContainer {
		
		private final int originalIndex;
		private final ActionProperty actionProperty;
	}
}
