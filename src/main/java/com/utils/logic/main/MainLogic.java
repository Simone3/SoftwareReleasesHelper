package com.utils.logic.main;

import java.util.List;

import com.utils.logic.common.StepLogic;
import com.utils.logic.git.GitLogic;
import com.utils.logic.jenkins.JenkinsLogic;
import com.utils.model.main.Action;
import com.utils.model.main.Category;
import com.utils.model.main.MainStep;
import com.utils.model.main.Project;
import com.utils.model.properties.Properties;
import com.utils.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainLogic extends StepLogic<MainStep> {
	
	private final Properties properties;
	private final CommandLineInterface cli;
	private final GitLogic gitLogic;
	private final JenkinsLogic jenkinsLogic;
	
	private Category category;
	private int currentProjectIndex = 0;
	private List<Project> projects;
	
	public MainLogic(Properties properties, CommandLineInterface cli) {
		
		super(MainStep.PICK_CATEGORY, MainStep.EXIT);
		this.properties = properties;
		this.cli = cli;
		this.gitLogic = new GitLogic(properties, cli);
		this.jenkinsLogic = new JenkinsLogic(properties, cli);
	}

	public void execute() {
		
		try {
		
			cli.printTitle("START");
			loopSteps();
			cli.printTitle(" END ");
		}
		catch(Exception e) {
			
			cli.printSeparator();
			cli.printError("UNEXPECTED GENERIC EXCEPTION: %s", e.getMessage());
			log.error("Generic error in main logic", e);
		}
	}

	@Override
	protected MainStep processCurrentStep(MainStep currentStep) {
		
		switch(currentStep) {
		
			case PICK_CATEGORY:
				return pickCategory();
		
			case PICK_PROJECTS:
				return pickProjects();
		
			case DO_ACTIONS:
				return doActions();
				
			default:
				throw new IllegalStateException("Unknown step: " + currentStep);
		}
	}
	
	private MainStep pickCategory() {
		
		var allCategories = properties.getCategories();
		var optionalPreSelection = properties.getOptionalPreSelectedCategoryIndex();
		
		category = cli.askUserSelection("Categories", allCategories, optionalPreSelection);
		cli.printSeparator();
		
		return MainStep.PICK_PROJECTS;
	}
	
	private MainStep pickProjects() {
		
		var allProjects = category.getProjects();
		var optionalPreSelection = properties.getOptionalPreSelectedProjectIndices();
		
		projects = cli.askUserSelectionMultiple("Projects", allProjects, true, optionalPreSelection);
		cli.printSeparator();
		
		currentProjectIndex = 0;
		
		return MainStep.DO_ACTIONS;
	}
	
	private MainStep doActions() {
		
		Project project = projects.get(currentProjectIndex);

		for(var i = 0; i < project.getActions().size(); i++) {
			
			var action = project.getActions().get(i);
			var actionDescription = "project \"" + project.getName() + "\", action #" + (i + 1) + " (" + action.getActionType() + ")";

			cli.println("Starting %s", actionDescription);
			cli.println();
			
			doAction(action);

			cli.println();
			cli.println("Completed %s", actionDescription);
			cli.println();
		}
		
		if(currentProjectIndex >= projects.size() - 1) {
			
			clearState();
			
			if(cli.askUserConfirmation("Restart from the beginning")) {

				cli.printSeparator();
				return MainStep.PICK_CATEGORY;
			}
			else {

				return MainStep.EXIT;
			}
		}
		else {
			
			currentProjectIndex++;
			return MainStep.DO_ACTIONS;
		}
	}

	private void doAction(Action action) {
		
		switch(action.getActionType()) {
		
			case GIT:
				gitLogic.execute(action);
				break;
				
			case JENKINS:
				jenkinsLogic.execute(action);
				break;
				
			default:
				throw new IllegalStateException("Unrecognized action " + action);
		}
	}

	private void clearState() {
		
		this.category = null;
		this.currentProjectIndex = 0;
		this.projects = null;
	}
}
