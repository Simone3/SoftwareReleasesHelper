package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.MavenCommandsAction;
import com.utils.releaseshelper.model.logic.maven.MavenCommand;
import com.utils.releaseshelper.model.service.git.GitCommitChangesServiceInput;
import com.utils.releaseshelper.model.service.git.GitPrepareForChangesServiceInput;
import com.utils.releaseshelper.model.service.maven.MavenCommandServiceModel;
import com.utils.releaseshelper.model.service.maven.MavenRunCommandServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.maven.MavenService;
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more generic Maven commands, optionally committing all changes (if any) to a Git repository (e.g. for "versions:set" plugin)
 */
public class MavenCommandsActionLogic extends ActionLogic<MavenCommandsAction> {

	private final MavenService mavenService;
	private final GitService gitService;
	
	private List<Map<String, String>> commandsArguments;
	private String commitMessage;

	protected MavenCommandsActionLogic(MavenCommandsAction action, Map<String, String> variables, CommandLineInterface cli, MavenService mavenService, GitService gitService) {
		
		super(action, variables, cli);
		this.mavenService = mavenService;
		this.gitService = gitService;
	}

	@Override
	protected void beforeAction() {
		
		defineArguments();
		defineCommitMessage();
	}

	@Override
	protected void printActionDescription() {
		
		String projectFolder = action.getProjectFolder();
		List<MavenCommand> commands = action.getCommands();
		
		cli.println("Run these Maven commands on %s:", projectFolder);
		
		for(int i = 0; i < commands.size(); i++) {
			
			MavenCommand command = commands.get(i);
			String goals = command.getGoals();
			Map<String, String> arguments = commandsArguments.get(i);
			
			if(arguments == null) {

				cli.println("  - %s", goals);
			}
			else {
				
				cli.println("  - %s with arguments:", goals);
				for(Entry<String, String> entry: arguments.entrySet()) {
					
					cli.println("    - %s: %s", entry.getKey(), entry.getValue());
				}
			}
		}

		if(action.getGitCommit() != null) {
			
			cli.println("And then commit all changes:");
			cli.println("  - Branch: %s", action.getGitCommit().getBranch());
			cli.println("  - Message: %s", commitMessage);
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Run Maven commands";
	}

	@Override
	protected void doRunAction() {
		
		// Prepare the Git repository, if necessary
		String originalBranch = null;
		if(action.getGitCommit() != null) {
			
			GitPrepareForChangesServiceInput gitPrepareForChangesInput = mapGitPrepareForChangesInput();
			originalBranch = gitService.prepareForChanges(gitPrepareForChangesInput);
			cli.println();
		}
		
		// Run the Maven commands
		MavenRunCommandServiceInput mavenCommandsInput = mapMavenCommandsInput();
		mavenService.runCommands(mavenCommandsInput);
		
		// Commit any changes to the Git repository, if necessary
		if(action.getGitCommit() != null) {

			cli.println();
			GitCommitChangesServiceInput gitCommitChangesInput = mapGitCommitChangesInput(originalBranch);
			gitService.commitChanges(gitCommitChangesInput);
		}
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}
	
	private void defineArguments() {
		
		commandsArguments = new ArrayList<>();
		
		for(MavenCommand command: action.getCommands()) {

			String goals = command.getGoals();
			List<VariableDefinition> argumentDefinitions = command.getArguments();
			
			if(!CollectionUtils.isEmpty(argumentDefinitions)) {
				
				Map<String, String> arguments = new LinkedHashMap<>();
				for(VariableDefinition argumentDefinition: argumentDefinitions) {
					
					String argumentValue = VariablesUtils.defineVariable(cli, "Define Maven " + goals + " argument", argumentDefinition, variables);
					arguments.put(argumentDefinition.getKey(), argumentValue);
				}
				commandsArguments.add(arguments);
			}
			else {
				
				commandsArguments.add(null);
			}
		}
	}
	
	private void defineCommitMessage() {
		
		if(action.getGitCommit() != null) {
			
			commitMessage = VariablesUtils.replaceVariablePlaceholders(action.getGitCommit().getMessage(), variables, null);
		}
		else {
			
			commitMessage = null;
		}
	}
	
	private GitPrepareForChangesServiceInput mapGitPrepareForChangesInput() {
		
		GitPrepareForChangesServiceInput input = new GitPrepareForChangesServiceInput();
		input.setRepositoryFolder(action.getProjectFolder());
		input.setBranch(action.getGitCommit().getBranch());
		input.setPull(action.getGitCommit().isPull());
		return input;
	}

	private MavenRunCommandServiceInput mapMavenCommandsInput() {
		
		MavenRunCommandServiceInput input = new MavenRunCommandServiceInput();
		input.setProjectFolder(action.getProjectFolder());
		input.setCommands(mapMavenCommandModels());
		return input;
	}

	private List<MavenCommandServiceModel> mapMavenCommandModels() {
		
		List<MavenCommandServiceModel> serviceCommands = new ArrayList<>();
		
		List<MavenCommand> commands = action.getCommands();

		for(int i = 0; i < commands.size(); i++) {
			
			MavenCommand command = commands.get(i);
			Map<String, String> arguments = commandsArguments.get(i);
			
			serviceCommands.add(mapMavenCommandModel(command, arguments));
		}
		
		return serviceCommands;
	}

	private MavenCommandServiceModel mapMavenCommandModel(MavenCommand command, Map<String, String> arguments) {
		
		MavenCommandServiceModel serviceCommand = new MavenCommandServiceModel();
		serviceCommand.setGoals(command.getGoals());
		serviceCommand.setArguments(arguments);
		serviceCommand.setPrintMavenOutput(command.isPrintMavenOutput());
		return serviceCommand;
	}

	private GitCommitChangesServiceInput mapGitCommitChangesInput(String originalBranch) {
		
		GitCommitChangesServiceInput input = new GitCommitChangesServiceInput();
		input.setRepositoryFolder(action.getProjectFolder());
		input.setOriginalBranch(originalBranch);
		input.setMessage(commitMessage);
		return input;
	}
}
