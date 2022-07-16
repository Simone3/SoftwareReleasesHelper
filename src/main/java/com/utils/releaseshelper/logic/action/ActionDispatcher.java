package com.utils.releaseshelper.logic.action;

import java.util.Map;

import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.action.JenkinsBuildAction;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.logic.action.WaitAction;
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
		else if(action instanceof WaitAction) {
			
			return new WaitActionLogic((WaitAction) action, variables, cli);
		}
		else {
			
			throw new IllegalStateException("Unrecognized action " + action);
		}
	}
}
