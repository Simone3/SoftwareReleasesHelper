package com.utils.releaseshelper.mapping.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.domain.Action;
import com.utils.releaseshelper.model.domain.Config;
import com.utils.releaseshelper.model.domain.GitCommit;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.GitMergesAction;
import com.utils.releaseshelper.model.domain.GitPullAllAction;
import com.utils.releaseshelper.model.domain.JenkinsBuildAction;
import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.domain.OperatingSystemCommand;
import com.utils.releaseshelper.model.domain.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.domain.VariableDefinition;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.model.properties.ActionProperty;
import com.utils.releaseshelper.model.properties.ActionTypeProperty;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitMergesDefinitionProperty;
import com.utils.releaseshelper.model.properties.GitPullAllDefinitionProperty;
import com.utils.releaseshelper.model.properties.JenkinsBuildDefinitionProperty;
import com.utils.releaseshelper.model.properties.JenkinsParameterProperty;
import com.utils.releaseshelper.model.properties.OperatingSystemCommandProperty;
import com.utils.releaseshelper.model.properties.OperatingSystemCommandsDefinitionProperty;
import com.utils.releaseshelper.model.properties.VariableDefinitionProperty;
import com.utils.releaseshelper.utils.FileUtils;
import com.utils.releaseshelper.utils.UrlUtils;
import com.utils.releaseshelper.utils.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates action properties
 */
@UtilityClass
public class ActionMapperValidator {

	public static List<Action> mapAndValidateActions(List<ActionProperty> actionProperties, Config config) {
		
		ValidationUtils.notEmpty(actionProperties, "At least one action should be defined");
		
		Set<String> usedNames = new HashSet<>();
		List<Action> actions = new ArrayList<>();
		
		for(int i = 0; i < actionProperties.size(); i++) {
			
			ActionProperty actionProperty = actionProperties.get(i);
			
			Action action;
			try {
				
				action = mapAndValidateAction(actionProperty, config);
				
				String name = action.getName();
				if(usedNames.contains(name)) {
					
					throw new ValidationException("An action already has the \"" + action.getName() + "\" name");
				}
				usedNames.add(action.getName());
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid action at index " + i + " -> " + e.getMessage(), e);
			}
			
			actions.add(action);
		}
		
		return actions;
	}

	public static Action mapAndValidateAction(ActionProperty actionProperty, Config config) {
		
		ValidationUtils.notNull(actionProperty, "Action is empty");
		
		ActionTypeProperty type = ValidationUtils.notNull(actionProperty.getType(), "Action does not have a type");

		switch(type) {
				
			case JENKINS_BUILD:
				return mapAndValidateJenkinsBuildAction(actionProperty, config.getJenkins());
				
			case GIT_MERGES:
				return mapAndValidateGitMergesAction(actionProperty, config.getGit());
				
			case GIT_PULL_ALL:
				return mapAndValidateGitPullAllAction(actionProperty, config.getGit());
				
			case OPERATING_SYSTEM_COMMANDS:
				return mapAndValidateOperatingSystemCommandsAction(actionProperty, config.getGit());
				
			default:
				throw new ValidationException("Action has an unknown type: " + type);
		}
	}

	private static void mapAndValidateGenericAction(ActionProperty actionProperty, Action action) {
		
		String name = ValidationUtils.notBlank(actionProperty.getName(), "Action does not have a name");
		
		List<VariableDefinitionProperty> variablesProperty = actionProperty.getVariables();
		
		List<VariableDefinition> variables = null;
		if(!CollectionUtils.isEmpty(variablesProperty)) {
			
			try {
				
				variables = VariablesMapperValidator.mapAndValidateVariableDefinitions(variablesProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Action has an invalid list of variables -> " + e.getMessage(), e);
			}
		}
		
		action.setName(name);
		action.setVariables(variables == null ? List.of() : variables);
	}

	private static JenkinsBuildAction mapAndValidateJenkinsBuildAction(ActionProperty actionProperty, JenkinsConfig jenkinsConfig) {
		
		ValidationUtils.notNull(jenkinsConfig, "There are no global Jenkins configs");
		
		JenkinsBuildAction action = new JenkinsBuildAction();
		mapAndValidateGenericAction(actionProperty, action);
		
		JenkinsBuildDefinitionProperty buildDefinition = ValidationUtils.notNull(actionProperty.getJenkinsBuildDefinition(), "Jenkins build action does not have a build definition");
		
		String urlProperty = ValidationUtils.notBlank(buildDefinition.getUrl(), "Jenkins build action does not have a URL");
		List<JenkinsParameterProperty> parameterProperties = buildDefinition.getParameters();

		String fullUrl = UrlUtils.getFullUrl(jenkinsConfig.getBaseUrl(), urlProperty);
		List<KeyValuePair> parameters = JenkinsMapperValidator.mapAndValidateJenkinsParameters(parameterProperties);

		action.setUrl(fullUrl);
		action.setParameters(parameters);
		
		return action;
	}

	private static GitMergesAction mapAndValidateGitMergesAction(ActionProperty actionProperty, GitConfig gitConfig) {
		
		ValidationUtils.notNull(gitConfig, "There are no global Git configs");
		
		GitMergesAction action = new GitMergesAction();
		mapAndValidateGenericAction(actionProperty, action);
		
		GitMergesDefinitionProperty mergesDefinition = ValidationUtils.notNull(actionProperty.getGitMergesDefinition(), "Git merges action does not have a merges definition property");

		String repositoryFolderProperty = ValidationUtils.notBlank(mergesDefinition.getRepositoryFolder(), "Git merges action does not have a repository folder");
		String mergesProperties = ValidationUtils.notBlank(mergesDefinition.getMerges(), "Git merges action does not have the branches definition");
		Boolean pullProperty = mergesDefinition.getPull();
		
		String fullRepositoryFolder = FileUtils.getFullPath(gitConfig.getBasePath(), repositoryFolderProperty);
		
		action.setRepositoryFolder(fullRepositoryFolder);
		action.setMerges(mergesProperties);
		action.setPull(pullProperty != null && pullProperty);
		
		return action;
	}

	private static GitPullAllAction mapAndValidateGitPullAllAction(ActionProperty actionProperty, GitConfig gitConfig) {
		
		ValidationUtils.notNull(gitConfig, "There are no global Git configs");
		
		GitPullAllAction action = new GitPullAllAction();
		mapAndValidateGenericAction(actionProperty, action);
		
		GitPullAllDefinitionProperty pullAllDefinition = ValidationUtils.notNull(actionProperty.getGitPullAllDefinition(), "Git pull all action does not have a pull definition property");

		String parentFolderProperty = ValidationUtils.notBlank(pullAllDefinition.getParentFolder(), "Git pull all action does not have a parent folder");
		Boolean skipIfWorkingTreeDirty = pullAllDefinition.getSkipIfWorkingTreeDirty();
		
		action.setParentFolder(parentFolderProperty);
		action.setSkipIfWorkingTreeDirty(skipIfWorkingTreeDirty != null && skipIfWorkingTreeDirty);
		
		return action;
	}
	
	private static OperatingSystemCommandsAction mapAndValidateOperatingSystemCommandsAction(ActionProperty actionProperty, GitConfig gitConfig) {
		
		OperatingSystemCommandsAction action = new OperatingSystemCommandsAction();
		mapAndValidateGenericAction(actionProperty, action);
		
		OperatingSystemCommandsDefinitionProperty osCommandsDefinition = ValidationUtils.notNull(actionProperty.getOsCommandsDefinition(), "OS commands action does not have a commands definition property");

		String folderProperty = ValidationUtils.notBlank(osCommandsDefinition.getFolder(), "OS commands action does not have a folder");
		List<OperatingSystemCommandProperty> commandsProperties = osCommandsDefinition.getCommands();
		GitCommitProperty gitCommitProperty = osCommandsDefinition.getGitCommit();
		
		List<OperatingSystemCommand> commands = OperatingSystemMapperValidator.mapAndValidateOperatingSystemCommands(commandsProperties);
		GitCommit gitCommit = gitCommitProperty == null ? null : GitMapperValidator.mapAndValidateGitCommit(gitCommitProperty);
		
		if(gitCommit != null) {
			
			ValidationUtils.notNull(gitConfig, "There are no global Git configs");
		}
		
		action.setFolder(folderProperty);
		action.setCommands(commands);
		action.setGitCommit(gitCommit);
		
		return action;
	}
}
