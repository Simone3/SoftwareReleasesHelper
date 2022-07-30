package com.utils.releaseshelper.logic.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.step.RunActionsForEachProjectStep;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the step to run one or more actions for one or more projects
 */
public class RunActionsForEachProjectStepLogic extends StepLogic<RunActionsForEachProjectStep> {

	private static final String PROJECT_NAME_VARIABLE = "project-name";
	
	public static final String DEFAULT_ACTIONS_KEY = "default";

	public RunActionsForEachProjectStepLogic(RunActionsForEachProjectStep step, List<Project> pickedProjects, Map<String, String> variables, MainService mainService, CommandLineInterface cli) {
		
		super(step, pickedProjects, variables, mainService, cli);
	}

	@Override
	public void doRunStep() {
		
		Map<String, List<Action>> projectActionsMap = step.getProjectActions();
		List<Action> defaultActionsList = projectActionsMap.get(DEFAULT_ACTIONS_KEY);
		
		Map<String, String> originalVariables = new HashMap<>(variables);

		for(Project pickedProject: pickedProjects) {
			
			String projectName = pickedProject.getName();
			
			variables.put(PROJECT_NAME_VARIABLE, projectName);
			
			// Get the list of actions for the current project
			List<Action> actions = projectActionsMap.get(projectName);
			if(actions == null) {
				
				actions = defaultActionsList;
			}
			
			// Run all project actions
			for(Action action: actions) {
				
				String actionDescription = "action \"" + action.getName() + "\" (" + action.getTypeDescription() + ") for \"" + projectName + "\"";
				
				cli.startIndentationGroup("Start %s", actionDescription);
				
				try {
					
					runAction(action);
				}
				finally {
				
					cli.endIndentationGroup("End %s", actionDescription);
				}
			}
			
			// Reset variables after each project for variable scope
			variables.clear();
			variables.putAll(originalVariables);
		}
	}
}
