package com.utils.releaseshelper.logic.procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.utils.releaseshelper.logic.Logic;
import com.utils.releaseshelper.logic.step.PickProjectsStepLogic;
import com.utils.releaseshelper.logic.step.RunActionsForEachProjectStepLogic;
import com.utils.releaseshelper.logic.step.RunActionsStepLogic;
import com.utils.releaseshelper.logic.step.StepLogic;
import com.utils.releaseshelper.model.logic.Procedure;
import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.step.PickProjectsStep;
import com.utils.releaseshelper.model.logic.step.RunActionsForEachProjectStep;
import com.utils.releaseshelper.model.logic.step.RunActionsStep;
import com.utils.releaseshelper.model.logic.step.Step;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic implementation for a procedure
 * Its purpose is to run the business logic of a specific procedure
 * It is a stateful class (per procedure)
 */
@Slf4j
public class ProcedureLogic implements Logic {
	
	protected final Procedure procedure;
	protected final CommandLineInterface cli;
	private final MainService mainService;
	
	public ProcedureLogic(Procedure procedure, MainService mainService, CommandLineInterface cli) {
		
		Assert.notNull(procedure, "Procedure cannot be null");
		
		this.procedure = procedure;
		this.cli = cli;
		this.mainService = mainService;
	}
	
	public void run() {

		List<Project> pickedProjects = new ArrayList<>();
		Map<String, String> variables = new HashMap<>();
		
		List<Step> steps = procedure.getSteps();
		
		for(int i = 0; i < steps.size(); i++) {
			
			Step step = steps.get(i);
			
			String stepDescription = "step #" + i + " (" + step.getTypeDescription() + ") of \"" + procedure.getName() + "\"";
			
			cli.startIndentationGroup("Start %s", stepDescription);
			
			try {
			
				runStep(step, pickedProjects, variables);
			}
			catch(Exception e) {
				
				log.error("Step error", e);
				cli.printError("Interrupting all procedure steps because of error: %s", e.getMessage());
				return;
			}
			finally {
			
				cli.endIndentationGroup("End %s", stepDescription);
			}
		}
	}
	
	private void runStep(Step step, List<Project> pickedProjects, Map<String, String> variables) {
		
		StepLogic<?> stepLogic = getStepLogic(step, pickedProjects, variables);
		stepLogic.run();
	}
	
	private StepLogic<?> getStepLogic(Step step, List<Project> pickedProjects, Map<String, String> variables) {
		
		if(step instanceof RunActionsStep) {
			
			return new RunActionsStepLogic((RunActionsStep) step, pickedProjects, variables, mainService, cli);
		}
		else if(step instanceof PickProjectsStep) {
			
			return new PickProjectsStepLogic((PickProjectsStep) step, pickedProjects, variables, mainService, cli);
		}
		else if(step instanceof RunActionsForEachProjectStep) {
			
			return new RunActionsForEachProjectStepLogic((RunActionsForEachProjectStep) step, pickedProjects, variables, mainService, cli);
		}
		else {
			
			throw new IllegalStateException("Unrecognized step " + step);
		}
	}
}
