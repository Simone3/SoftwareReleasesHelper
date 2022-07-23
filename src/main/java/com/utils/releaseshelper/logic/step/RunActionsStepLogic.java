package com.utils.releaseshelper.logic.step;

import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.step.RunActionsStep;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the step to run one or more actions
 */
public class RunActionsStepLogic extends StepLogic<RunActionsStep> {

	public RunActionsStepLogic(RunActionsStep step, List<Project> pickedProjects, Map<String, String> variables, MainService mainService, CommandLineInterface cli) {
		
		super(step, pickedProjects, variables, mainService, cli);
	}

	@Override
	public void doRunStep() {
		
		for(Action action: step.getActions()) {
			
			String actionDescription = "action \"" + action.getName() + "\" (" + action.getTypeDescription() + ")";
			
			cli.startIdentationGroup("Start %s", actionDescription);
			
			runAction(action);
			
			cli.endIdentationGroup("End %s", actionDescription);
		}
	}
}
