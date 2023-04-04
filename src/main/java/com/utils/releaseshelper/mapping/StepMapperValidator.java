package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.utils.releaseshelper.logic.step.RunActionsForEachProjectStepLogic;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.step.PickProjectsStep;
import com.utils.releaseshelper.model.logic.step.RunActionsForEachProjectStep;
import com.utils.releaseshelper.model.logic.step.RunActionsStep;
import com.utils.releaseshelper.model.logic.step.Step;
import com.utils.releaseshelper.model.properties.StepProperty;
import com.utils.releaseshelper.model.properties.StepTypeProperty;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates step properties
 */
@UtilityClass
public class StepMapperValidator {

	public static List<Step> mapAndValidateSteps(List<StepProperty> stepsProperties, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notEmpty(stepsProperties, "At least one step should be defined");
		
		List<Step> steps = new ArrayList<>();
		
		Set<String> selectableProjectsHelper = new HashSet<>();
		
		for(int i = 0; i < stepsProperties.size(); i++) {

			StepProperty stepProperty = stepsProperties.get(i);
			
			Step step;
			try {
				
				step = mapAndValidateStep(stepProperty, actionDefinitions, selectableProjectsHelper);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid step at index " + i + " -> " + e.getMessage(), e);
			}

			steps.add(step);
		}
		
		return steps;
	}
	
	public static Step mapAndValidateStep(StepProperty stepProperty, Map<String, Action> actionDefinitions, Set<String> selectableProjectsHelper) {
		
		ValidationUtils.notNull(stepProperty, "Step is empty");
		
		StepTypeProperty type = ValidationUtils.notNull(stepProperty.getType(), "Step does not have a type");

		switch(type) {
		
			case RUN_ACTIONS:
				return mapAndValidateRunActionsStep(stepProperty, actionDefinitions);
				
			case PICK_PROJECTS:
				return mapAndValidatePickProjectsStep(stepProperty, selectableProjectsHelper);
				
			case RUN_ACTIONS_FOR_EACH_PROJECT:
				return mapAndValidateRunActionsForEachProjectStep(stepProperty, actionDefinitions, selectableProjectsHelper);
				
			default:
				throw new ValidationException("Step has an unknown type: " + type);
		}
	}
	
	private static void mapAndValidateGenericStep(StepProperty stepProperty, Step step) {
		
		// Nothing here for now
	}
	
	private static RunActionsStep mapAndValidateRunActionsStep(StepProperty stepProperty, Map<String, Action> actionDefinitions) {
		
		List<String> actionsProperty = stepProperty.getActions();
		
		List<Action> actions;
		try {
			
			actions = ActionMapperValidator.mapAndValidateProjectActions(actionsProperty, actionDefinitions);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid actions for step -> " + e.getMessage(), e);
		}
		
		RunActionsStep step = new RunActionsStep();
		mapAndValidateGenericStep(stepProperty, step);
		step.setActions(actions);
		return step;
	}
	
	private static PickProjectsStep mapAndValidatePickProjectsStep(StepProperty stepProperty, Set<String> selectableProjectsHelper) {
		
		String customPromptProperty = stepProperty.getCustomPrompt();
		List<String> projectsProperty = stepProperty.getProjects();

		List<Project> projects;
		try {
			
			projects = ProjectMapperValidator.mapAndValidateProjects(projectsProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid projects for step -> " + e.getMessage(), e);
		}
		
		// Save all projects to validate the project-actions map in "RUN_ACTIONS_FOR_EACH_PROJECT" step
		selectableProjectsHelper.clear();
		selectableProjectsHelper.addAll(projects.stream().map(Project::getName).collect(Collectors.toList()));
		
		PickProjectsStep step = new PickProjectsStep();
		mapAndValidateGenericStep(stepProperty, step);
		step.setCustomPrompt(customPromptProperty);
		step.setProjects(projects);
		return step;
	}
	
	private static RunActionsForEachProjectStep mapAndValidateRunActionsForEachProjectStep(StepProperty stepProperty, Map<String, Action> actionDefinitions, Set<String> selectableProjectsHelper) {
		
		Map<String, List<String>> projectActionsProperty = ValidationUtils.notEmpty(stepProperty.getProjectActions(), "Step does not have any mapping of actions by project");
		
		Map<String, List<Action>> projectActions = new HashMap<>();
		for(Entry<String, List<String>> entry: projectActionsProperty.entrySet()) {
			
			String nameProperty = ValidationUtils.notBlank(entry.getKey(), "Step has an empty project name");
			List<String> actionsProperty = entry.getValue();
			
			try {
				
				List<Action> actions = ActionMapperValidator.mapAndValidateProjectActions(actionsProperty, actionDefinitions);
				projectActions.put(nameProperty, actions);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid actions for step (project name " + nameProperty + ") -> " + e.getMessage(), e);
			}
		}
		
		// Check that all projects have a corresponding key in the map
		boolean hasDefault = projectActions.containsKey(RunActionsForEachProjectStepLogic.DEFAULT_ACTIONS_KEY);
		for(String selectableProject: selectableProjectsHelper) {
			
			ValidationUtils.isTrue(projectActions.containsKey(selectableProject) || hasDefault, "Invalid projects for step -> Project " + selectableProject + " is not mapped and no default is present");
		}

		RunActionsForEachProjectStep step = new RunActionsForEachProjectStep();
		mapAndValidateGenericStep(stepProperty, step);
		step.setProjectActions(projectActions);
		return step;
	}
}
