package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;
import com.utils.releaseshelper.model.service.git.GitCommitChangesServiceInput;
import com.utils.releaseshelper.model.service.git.GitPrepareForChangesServiceInput;
import com.utils.releaseshelper.model.service.maven.MavenCommandServiceModel;
import com.utils.releaseshelper.model.service.maven.MavenRunCommandServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.maven.MavenService;
import com.utils.releaseshelper.utils.ValuesDefiner;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more generic Maven commands, optionally committing all changes (if any) to a Git repository (e.g. for "versions:set" plugin)
 */
public class MavenCommandsActionLogic extends ActionLogic<MavenCommandsAction> {

	private final MavenService mavenService;
	private final GitService gitService;

	protected MavenCommandsActionLogic(MavenCommandsAction action, Map<String, String> variables, CommandLineInterface cli, MavenService mavenService, GitService gitService) {
		
		super(action, variables, cli);
		this.mavenService = mavenService;
		this.gitService = gitService;
	}

	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {

		List<MavenCommand> commands = action.getCommands();
		for(MavenCommand command: commands) {
			
			valuesDefiner.addValueDefinition(command.getGoals(), "goals");
			
			List<VariableDefinition> arguments = command.getArguments();
			for(VariableDefinition argument: arguments) {
				
				valuesDefiner.addValueDefinition(argument.getValueDefinition(), argument.getKey() + " argument");
			}
		}

		GitCommit gitCommit = action.getGitCommit();
		if(gitCommit != null) {
			
			valuesDefiner.addValueDefinition(gitCommit.getBranch(), "Git branch");
			valuesDefiner.addValueDefinition(gitCommit.getMessage(), "Git message");
		}
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		String projectFolder = action.getProjectFolder();
		List<MavenCommand> commands = action.getCommands();
		
		cli.println("Run these Maven commands on %s:", projectFolder);
		
		for(MavenCommand command: commands) {
			
			String goals = valuesDefiner.getValue(command.getGoals());
			List<VariableDefinition> arguments = command.getArguments();
			
			if(CollectionUtils.isEmpty(arguments)) {

				cli.println("  - %s", goals);
			}
			else {
				
				cli.println("  - %s with arguments:", goals);
				for(VariableDefinition argument: arguments) {
					
					cli.println("    - %s: %s", argument.getKey(), valuesDefiner.getValue(argument.getValueDefinition()));
				}
			}
		}

		GitCommit gitCommit = action.getGitCommit();
		if(gitCommit != null) {
			
			cli.println("And then commit all changes:");
			cli.println("  - Branch: %s", valuesDefiner.getValue(gitCommit.getBranch()));
			cli.println("  - Message: %s", valuesDefiner.getValue(gitCommit.getMessage()));
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Run Maven commands";
	}

	@Override
	protected void doRunAction(ValuesDefiner valuesDefiner) {
		
		// Prepare the Git repository, if necessary
		String originalBranch = null;
		if(action.getGitCommit() != null) {
			
			GitPrepareForChangesServiceInput gitPrepareForChangesInput = mapGitPrepareForChangesInput(valuesDefiner);
			originalBranch = gitService.prepareForChanges(gitPrepareForChangesInput);
			cli.println();
		}
		
		// Run the Maven commands
		MavenRunCommandServiceInput mavenCommandsInput = mapMavenCommandsInput(valuesDefiner);
		mavenService.runCommands(mavenCommandsInput);
		
		// Commit any changes to the Git repository, if necessary
		if(action.getGitCommit() != null) {

			cli.println();
			GitCommitChangesServiceInput gitCommitChangesInput = mapGitCommitChangesInput(valuesDefiner, originalBranch);
			gitService.commitChanges(gitCommitChangesInput);
		}
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}

	private GitPrepareForChangesServiceInput mapGitPrepareForChangesInput(ValuesDefiner valuesDefiner) {
		
		GitPrepareForChangesServiceInput input = new GitPrepareForChangesServiceInput();
		input.setRepositoryFolder(action.getProjectFolder());
		input.setBranch(valuesDefiner.getValue(action.getGitCommit().getBranch()));
		input.setPull(action.getGitCommit().isPull());
		return input;
	}

	private MavenRunCommandServiceInput mapMavenCommandsInput(ValuesDefiner valuesDefiner) {
		
		MavenRunCommandServiceInput input = new MavenRunCommandServiceInput();
		input.setProjectFolder(action.getProjectFolder());
		input.setCommands(mapMavenCommandModels(valuesDefiner));
		return input;
	}

	private List<MavenCommandServiceModel> mapMavenCommandModels(ValuesDefiner valuesDefiner) {
		
		List<MavenCommandServiceModel> serviceCommands = new ArrayList<>();
		
		List<MavenCommand> commands = action.getCommands();

		for(MavenCommand command: commands) {
			
			String goals = valuesDefiner.getValue(command.getGoals());
			List<VariableDefinition> arguments = command.getArguments();
			
			serviceCommands.add(mapMavenCommandModel(valuesDefiner, command, goals, arguments));
		}
		
		return serviceCommands;
	}

	private MavenCommandServiceModel mapMavenCommandModel(ValuesDefiner valuesDefiner, MavenCommand command, String goals, List<VariableDefinition> arguments) {
		
		Map<String, String> argumentsMap = new HashMap<>();
		for(VariableDefinition argument: arguments) {
			
			argumentsMap.put(argument.getKey(), valuesDefiner.getValue(argument.getValueDefinition()));
		}
		
		MavenCommandServiceModel serviceCommand = new MavenCommandServiceModel();
		serviceCommand.setGoals(goals);
		serviceCommand.setArguments(argumentsMap);
		serviceCommand.setSuppressOutput(command.isSuppressOutput());
		serviceCommand.setOffline(command.isOffline());
		return serviceCommand;
	}

	private GitCommitChangesServiceInput mapGitCommitChangesInput(ValuesDefiner valuesDefiner, String originalBranch) {
		
		GitCommitChangesServiceInput input = new GitCommitChangesServiceInput();
		input.setRepositoryFolder(action.getProjectFolder());
		input.setOriginalBranch(originalBranch);
		input.setMessage(valuesDefiner.getValue(action.getGitCommit().getMessage()));
		return input;
	}
}
