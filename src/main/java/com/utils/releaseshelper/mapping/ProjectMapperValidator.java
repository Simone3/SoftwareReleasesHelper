package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.properties.ProjectProperty;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates project properties
 */
@UtilityClass
public class ProjectMapperValidator {

	public static List<Project> mapAndValidateProjects(List<ProjectProperty> projectsProperties, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notEmpty(projectsProperties, "At least one project should be defined");
		
		List<Project> projects = new ArrayList<>();
		Map<String, Void> projectNames = new HashMap<>();
		
		for(int i = 0; i < projectsProperties.size(); i++) {

			ProjectProperty projectProperty = projectsProperties.get(i);
			
			Project project;
			try {
				
				project = mapAndValidateProject(projectProperty, actionDefinitions);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid project at index " + i + " -> " + e.getMessage());
			}

			String projectName = projectProperty.getName();
			if(projectNames.containsKey(projectName)) {

				throw new ValidationException("Project at index " + i + " has the same name of a previous project");
			}
			
			projectNames.put(projectName, null);
			projects.add(project);
		}
		
		return projects;
	}
	
	public static Project mapAndValidateProject(ProjectProperty projectProperty, Map<String, Action> actionDefinitions) {
		
		String name = ValidationUtils.notBlank(projectProperty.getName(), "Project does not have a name");
		List<Action> actions = ActionMapperValidator.mapAndValidateProjectActions(projectProperty.getActionNames(), actionDefinitions);
		
		Project project = new Project();
		project.setName(name);
		project.setActions(actions);
		return project;
	}
}
