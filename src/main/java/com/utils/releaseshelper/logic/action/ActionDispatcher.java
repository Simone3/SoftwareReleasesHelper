package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.ChainAction;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.jenkins.JenkinsService;
import com.utils.releaseshelper.service.maven.MavenService;
import com.utils.releaseshelper.service.process.OperatingSystemService;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.RequiredArgsConstructor;

/**
 * Dispatcher for each action
 * Its purpose is to run the appropriate Logic for each type of action
 */
@RequiredArgsConstructor
public class ActionDispatcher {

	private final CommandLineInterface cli;
	private final GitService gitService;
	private final JenkinsService jenkinsService;
	private final MavenService mavenService;
	private final OperatingSystemService operatingSystemService;

	public void dispatch(Action action, Map<String, String> variables) {
		
		ActionLogic<?> actionLogic = getActionLogic(action, variables);
		actionLogic.run();
	}
	
	private ActionLogic<?> getActionLogic(Action action, Map<String, String> variables) {
		
		if(action instanceof DefineVariablesAction) {
			
			return new DefineVariablesActionLogic((DefineVariablesAction) action, variables, cli);
		}
		else if(action instanceof MavenCommandsAction) {
			
			return new MavenCommandsActionLogic((MavenCommandsAction) action, variables, cli, mavenService, gitService);
		}
		else if(action instanceof OperatingSystemCommandsAction) {
			
			return new OperatingSystemCommandsActionLogic((OperatingSystemCommandsAction) action, variables, cli, operatingSystemService, gitService);
		}
		else if(action instanceof GitMergesAction) {
			
			return new GitMergesActionLogic((GitMergesAction) action, variables, cli, gitService);
		}
		else if(action instanceof JenkinsBuildAction) {
			
			return new JenkinsBuildActionLogic((JenkinsBuildAction) action, variables, cli, jenkinsService);
		}
		else if(action instanceof ChainAction) {
			
			return new ChainActionLogic((ChainAction) action, variables, cli, getChainActionSubActionsLogic((ChainAction) action, variables));
		}
		else {
			
			throw new IllegalStateException("Unrecognized action " + action);
		}
	}

	private List<ActionLogic<?>> getChainActionSubActionsLogic(ChainAction action, Map<String, String> variables) {
		
		List<ActionLogic<?>> subActionsLogic = new ArrayList<>();
		
		// This hopefully does not have cycles because we passed validation!
		for(Action subAction: action.getActions()) {
			
			subActionsLogic.add(getActionLogic(subAction, variables));
		}
		
		return subActionsLogic;
	}
}
