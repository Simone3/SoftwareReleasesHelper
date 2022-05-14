package com.utils.releaseshelper.logic.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.logic.action.ActionDispatcher;
import com.utils.releaseshelper.logic.common.StepLogic;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.config.MavenConfig;
import com.utils.releaseshelper.model.logic.ActionFlags;
import com.utils.releaseshelper.model.logic.Category;
import com.utils.releaseshelper.model.logic.MainLogicData;
import com.utils.releaseshelper.model.logic.MainStep;
import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.jenkins.JenkinsService;
import com.utils.releaseshelper.service.maven.MavenService;
import com.utils.releaseshelper.service.process.OperatingSystemService;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

/**
 * The main Logic class
 * It runs the main CLI program:
 * - pick categories step
 * - pick projects step
 * - run actions step
 */
@Slf4j
public class MainLogic extends StepLogic<MainStep> {

	private final MainLogicData mainLogicData;
	private final CommandLineInterface cli;
	private final ActionDispatcher actionDispatcher;
	
	private Category category;
	private List<Project> projects;
	
	public MainLogic(MainLogicData mainLogicData, CommandLineInterface cli) {
		
		super(MainStep.PICK_CATEGORY, MainStep.EXIT);

		this.mainLogicData = mainLogicData;
		this.cli = cli;
		
		Config config = mainLogicData.getConfig();
		ActionFlags actionFlags = mainLogicData.getActionFlags();
		
		GitService gitService = actionFlags.isGitActions() ? new GitService(config, cli) : null;
		JenkinsService jenkinsService =  actionFlags.isJenkinsActions() ? new JenkinsService(config, cli) : null;
		MavenService mavenService =  actionFlags.isMavenActions() ? new MavenService(config, cli) : null;
		OperatingSystemService operatingSystemService =  actionFlags.isOperatingSystemActions() ? new OperatingSystemService(config, cli) : null;
		
		this.actionDispatcher = new ActionDispatcher(cli, gitService, jenkinsService, mavenService, operatingSystemService);
	}

	public void execute() {
		
		try {
		
			printConfig();
			loopSteps();
		}
		catch(Exception e) {
			
			cli.printError("UNEXPECTED GENERIC EXCEPTION: %s", e.getMessage());
			log.error("Generic error in main logic", e);
		}
	}
	
	private void printConfig() {
		
		Config config = mainLogicData.getConfig();
		boolean printPasswords = config.isPrintPasswords();
		GitConfig git = config.getGit();
		JenkinsConfig jenkins = config.getJenkins();
		MavenConfig maven = config.getMaven();
		
		if(git != null) {

			String password = StringUtils.defaultIfBlank(git.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.println("Loaded Git config:");
			cli.println("  - Username: %s", StringUtils.defaultIfBlank(git.getUsername(), ""));
			cli.println("  - Password: %s", password);
			cli.println("  - Merge message: %s", StringUtils.defaultIfBlank(git.getMergeMessage(), ""));
			cli.println("  - Timeout (ms): %s", git.getTimeoutMilliseconds());
			cli.println();
		}
		
		if(jenkins != null) {

			String password = StringUtils.defaultIfBlank(jenkins.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.println("Loaded Jenkins config:");
			cli.println("  - Crumb URL: %s", StringUtils.defaultIfBlank(jenkins.getCrumbUrl(), ""));
			cli.println("  - Username: %s", StringUtils.defaultIfBlank(jenkins.getUsername(), ""));
			cli.println("  - Password: %s", password);
			cli.println("  - Insecure HTTPS: %s", jenkins.isInsecureHttps());
			cli.println("  - Timeout (ms): %s", jenkins.getTimeoutMilliseconds());
			cli.println();
		}
		
		if(maven != null) {

			cli.println("Loaded Maven config:");
			cli.println("  - Maven home: %s", StringUtils.defaultIfBlank(maven.getMavenHomeFolder(), ""));
			cli.println();
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
		
		List<Category> allCategories = mainLogicData.getCategories();
		String optionalPreSelection = mainLogicData.getOptionalPreSelectedCategoryIndex();
		
		category = cli.askUserSelection("Categories", allCategories, optionalPreSelection);
		
		return MainStep.PICK_PROJECTS;
	}
	
	private MainStep pickProjects() {
		
		List<Project> allProjects = category.getProjects();
		String optionalPreSelection = mainLogicData.getOptionalPreSelectedProjectIndices();
		
		projects = cli.askUserSelectionMultiple("Projects", allProjects, true, optionalPreSelection);
		
		return MainStep.DO_ACTIONS;
	}
	
	private MainStep doActions() {
		
		// Run each project
		for(Project project: projects) {

			String projectDescription = "project \"" + project.getName() + "\" of \"" + category.getName() + "\"";
			
			cli.startIdentationGroup("Start %s", projectDescription);
			
			doProjectActions(project);

			cli.endIdentationGroup("End %s", projectDescription);
		}
		
		// Clear the state for the next run
		clearState();
		
		// Ask confirmation before restarting if the user will not manually pick the category
		if(mainLogicData.getCategories().size() == 1 || !StringUtils.isBlank(mainLogicData.getOptionalPreSelectedCategoryIndex())) {
			
			if(cli.askUserConfirmation("Restart from the beginning")) {

				return MainStep.PICK_CATEGORY;
			}
			else {

				return MainStep.EXIT;
			}
		}
		else {
			
			return MainStep.PICK_CATEGORY;
		}
	}
	
	private void doProjectActions(Project project) {
		
		Map<String, String> projectVariables = new HashMap<>();
		
		for(int i = 0; i < project.getActions().size(); i++) {
			
			Action action = project.getActions().get(i);
			String actionDescription = "action \"" + action.getName() + "\" (" + action.getTypeDescription() + ")";
			
			cli.startIdentationGroup("Start %s", actionDescription);
			
			try {
				
				actionDispatcher.dispatch(action, projectVariables);
			}
			catch(Exception e) {
				
				log.error("Action error", e);
				cli.printError("Aborting all project actions because of error: %s", e.getMessage());
				return;
			}
			finally {
				
				cli.endIdentationGroup("End %s", actionDescription);
			}
		}
	}

	private void clearState() {
		
		this.category = null;
		this.projects = null;
	}
}
