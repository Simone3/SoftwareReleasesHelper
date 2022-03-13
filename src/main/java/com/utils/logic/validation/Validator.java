package com.utils.logic.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.main.Action;
import com.utils.model.main.Category;
import com.utils.model.main.Project;
import com.utils.model.properties.Properties;

import lombok.Data;

public class Validator extends ValidationErrorGenerator {

	private final GitValidator gitValidator;
	private final JenkinsValidator jenkinsValidator;
	
	public Validator() {
		
		this.gitValidator = new GitValidator();
		this.jenkinsValidator = new JenkinsValidator();
	}
	
	public void validateProperties(Properties properties) {
		
		if(properties == null) {
			
			throw topLevelError("No properties are defined");
		}

		var categories = properties.getCategories();
		var git = properties.getGit();
		var jenkins = properties.getJenkins();
		
		ValidationHelper helper = new ValidationHelper();
		validateCategories(helper, categories);
		
		if(helper.isGitActionPresent()) {
			
			gitValidator.validateGitData(git);
		}
		
		if(helper.isJenkinsActionPresent()) {
			
			jenkinsValidator.validateJenkinsData(jenkins);
		}
	}

	private void validateCategories(ValidationHelper helper, List<Category> categories) {
		
		if(categories == null || categories.isEmpty()) {
			
			throw topLevelError("At least one category should be defined");
		}
		
		Map<String, Void> categoryNames = new HashMap<>();
		
		for(var i = 0; i < categories.size(); i++) {
			
			Category category = categories.get(i);
			validateCategory(helper, category, i);
			
			String categoryName = category.getName();
			if(categoryNames.containsKey(categoryName)) {
				
				throw categoryError(categoryName, "has the same name of a previous category");
			}
			categoryNames.put(categoryName, null);
		}
	}

	private void validateCategory(ValidationHelper helper, Category category, int categoryIndex) {
		
		if(category == null) {
			
			throw categoryError(categoryIndex, "is empty");
		}
		
		var categoryName = category.getName();
		var projects = category.getProjects();
		
		if(StringUtils.isBlank(categoryName)) {
			
			throw categoryError(categoryIndex, "does not have a name");
		}
		
		validateProjects(helper, projects, categoryName);
	}

	private void validateProjects(ValidationHelper helper, List<Project> projects, String categoryName) {
		
		if(projects == null || projects.isEmpty()) {
			
			throw categoryError(categoryName, "does not have any project");
		}
		
		Map<String, Void> projectNames = new HashMap<>();
		
		for(var i = 0; i < projects.size(); i++) {
			
			Project project = projects.get(i);
			validateProject(helper, project, categoryName, i);
			
			String projectName = project.getName();
			if(projectNames.containsKey(projectName)) {
				
				throw projectError(categoryName, projectName, "has the same name of a previous project");
			}
			projectNames.put(projectName, null);
		}
	}

	private void validateProject(ValidationHelper helper, Project project, String categoryName, int projectIndex) {
		
		if(project == null) {
			
			throw projectError(categoryName, projectIndex, "is empty");
		}
		
		var projectName = project.getName();
		var actions = project.getActions();
		
		if(StringUtils.isBlank(projectName)) {
			
			throw projectError(categoryName, projectIndex, "does not have a name");
		}
		
		validateActions(helper, actions, categoryName, projectName);
	}

	private void validateActions(ValidationHelper helper, List<Action> actions, String categoryName, String projectName) {
		
		if(actions == null || actions.isEmpty()) {
			
			throw projectError(categoryName, projectName, "does not have any action");
		}
		
		for(var i = 0; i < actions.size(); i++) {
			
			validateAction(helper, actions.get(i), categoryName, projectName, i);
		}
	}

	private void validateAction(ValidationHelper helper, Action action, String categoryName, String projectName, int actionIndex) {
		
		if(action == null) {
			
			throw actionError(categoryName, projectName, actionIndex, "is empty");
		}
		
		var type = action.getActionType();
		
		if(type == null) {
			
			throw actionError(categoryName, projectName, actionIndex, "does not have a type");
		}
		
		switch(type) {
		
			case GIT:
				gitValidator.validateGitAction(action, categoryName, projectName, actionIndex);
				helper.setGitActionPresent(true);
				break;
				
			case JENKINS:
				jenkinsValidator.validateJenkinsAction(action, categoryName, projectName, actionIndex);
				helper.setJenkinsActionPresent(true);
				break;
				
			default:
				throw new IllegalStateException("Unrecognized action type " + type);
		}
	}
	
	@Data
	private static class ValidationHelper {
		
		private boolean gitActionPresent;
		private boolean jenkinsActionPresent;
	}
}
