package com.utils.releaseshelper.logic.step;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.utils.releaseshelper.logic.Logic;
import com.utils.releaseshelper.logic.action.ActionLogic;
import com.utils.releaseshelper.logic.action.DefineVariablesActionLogic;
import com.utils.releaseshelper.logic.action.GitMergesActionLogic;
import com.utils.releaseshelper.logic.action.JenkinsBuildActionLogic;
import com.utils.releaseshelper.logic.action.MavenCommandsActionLogic;
import com.utils.releaseshelper.logic.action.OperatingSystemCommandsActionLogic;
import com.utils.releaseshelper.logic.action.WaitActionLogic;
import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.ActionErrorRemediation;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.logic.action.WaitAction;
import com.utils.releaseshelper.model.logic.step.Step;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.utils.ErrorRemediationUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Generic logic implementation for a step
 * Its purpose is to run the business logic of a specific type of step
 * It is a stateful class (per step)
 * @param <S> the step type
 */
public abstract class StepLogic<S extends Step> implements Logic {
	
	protected final S step;
	protected final List<Project> pickedProjects;
	protected final Map<String, String> variables;
	protected final CommandLineInterface cli;
	protected final MainService mainService;
	
	protected StepLogic(S step, List<Project> pickedProjects, Map<String, String> variables, MainService mainService, CommandLineInterface cli) {
		
		Assert.notNull(step, "Step cannot be null");
		Assert.notNull(pickedProjects, "Picked projects cannot be null");
		Assert.notNull(variables, "Variables cannot be null");
		
		this.step = step;
		this.pickedProjects = pickedProjects;
		this.variables = variables;
		this.mainService = mainService;
		this.cli = cli;
	}
	
	public void run() {
		
		doRunStep();
	}
	
	protected abstract void doRunStep();
	
	protected void runAction(Action action) {
		
		ErrorRemediationUtils.runWithErrorRemediation(
			cli,
			"Error running action",
			ActionErrorRemediation.values(),
			() -> {
				
				ActionLogic<?> actionLogic = getActionLogic(action);
				actionLogic.run();
			}
		);
	}
	
	private ActionLogic<?> getActionLogic(Action action) {
		
		if(action instanceof DefineVariablesAction) {
			
			return new DefineVariablesActionLogic((DefineVariablesAction) action, variables, cli);
		}
		else if(action instanceof MavenCommandsAction) {
			
			return new MavenCommandsActionLogic((MavenCommandsAction) action, variables, cli, mainService.getMaven(), mainService.getGit());
		}
		else if(action instanceof OperatingSystemCommandsAction) {
			
			return new OperatingSystemCommandsActionLogic((OperatingSystemCommandsAction) action, variables, cli, mainService.getOperatingSystem(), mainService.getGit());
		}
		else if(action instanceof GitMergesAction) {
			
			return new GitMergesActionLogic((GitMergesAction) action, variables, cli, mainService.getGit());
		}
		else if(action instanceof JenkinsBuildAction) {
			
			return new JenkinsBuildActionLogic((JenkinsBuildAction) action, variables, cli, mainService.getJenkins());
		}
		else if(action instanceof WaitAction) {
			
			return new WaitActionLogic((WaitAction) action, variables, cli);
		}
		else {
			
			throw new IllegalStateException("Unrecognized action " + action);
		}
	}
}
