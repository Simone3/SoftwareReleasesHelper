package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates project properties
 */
@UtilityClass
public class ProjectMapperValidator {

	public static List<Project> mapAndValidateProjects(List<String> projectsProperties) {
		
		ValidationUtils.notEmpty(projectsProperties, "At least one project should be defined");
		
		List<Project> projects = new ArrayList<>();
		Set<String> projectNames = new HashSet<>();
		
		for(int i = 0; i < projectsProperties.size(); i++) {

			String projectProperty = projectsProperties.get(i);
			
			Project project;
			try {
				
				project = mapAndValidateProject(projectProperty);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid project at index " + i + " -> " + e.getMessage());
			}

			if(projectNames.contains(projectProperty)) {

				throw new ValidationException("Project at index " + i + " has the same name of a previous project");
			}
			
			projectNames.add(projectProperty);
			projects.add(project);
		}
		
		return projects;
	}
	
	public static Project mapAndValidateProject(String projectProperty) {
		
		String name = ValidationUtils.notBlank(projectProperty, "Project does not have a name");
		
		Project project = new Project();
		project.setName(name);
		return project;
	}
}
