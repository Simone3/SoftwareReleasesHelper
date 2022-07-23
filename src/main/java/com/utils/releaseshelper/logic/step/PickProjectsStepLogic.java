package com.utils.releaseshelper.logic.step;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.step.PickProjectsStep;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the step to pick one or more projects
 */
public class PickProjectsStepLogic extends StepLogic<PickProjectsStep> {

	public PickProjectsStepLogic(PickProjectsStep step, List<Project> pickedProjects, Map<String, String> variables, MainService mainService, CommandLineInterface cli) {
		
		super(step, pickedProjects, variables, mainService, cli);
	}

	@Override
	public void doRunStep() {
		
		pickedProjects.clear();
		
		String message = StringUtils.isBlank(step.getCustomPrompt()) ? "Pick one or more projects" : step.getCustomPrompt();
		
		List<Project> userSelection = cli.askUserSelectionMultiple(message, step.getProjects(), true, null, false);
		
		pickedProjects.addAll(userSelection);
	}
}
