package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.process.OperatingSystemCommand;
import com.utils.releaseshelper.model.service.git.GitCommitChangesServiceInput;
import com.utils.releaseshelper.model.service.git.GitPrepareForChangesServiceInput;
import com.utils.releaseshelper.model.service.process.OperatingSystemCommandServiceModel;
import com.utils.releaseshelper.model.service.process.OperatingSystemRunCommandServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.process.OperatingSystemService;
import com.utils.releaseshelper.utils.ValuesDefiner;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more generic operating system commands, optionally committing all changes (if any) to a Git repository
 */
public class OperatingSystemCommandsActionLogic extends ActionLogic<OperatingSystemCommandsAction> {

	private final OperatingSystemService operatingSystemService;
	private final GitService gitService;

	protected OperatingSystemCommandsActionLogic(OperatingSystemCommandsAction action, Map<String, String> variables, CommandLineInterface cli, OperatingSystemService operatingSystemService, GitService gitService) {
		
		super(action, variables, cli);
		this.operatingSystemService = operatingSystemService;
		this.gitService = gitService;
	}

	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {
		
		List<OperatingSystemCommand> commands = action.getCommands();
		for(OperatingSystemCommand command: commands) {
			
			valuesDefiner.addValueDefinition(command.getCommand(), "command");
		}
		
		GitCommit gitCommit = action.getGitCommit();
		if(gitCommit != null) {
			
			valuesDefiner.addValueDefinition(gitCommit.getBranch(), "Git branch");
			valuesDefiner.addValueDefinition(gitCommit.getMessage(), "Git message");
		}
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		String folder = action.getFolder();
		List<OperatingSystemCommand> commands = action.getCommands();
		
		cli.println("Run these commands in %s:", folder);
		
		for(OperatingSystemCommand command: commands) {
			
			cli.println("  - %s", valuesDefiner.getValue(command.getCommand()));
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
		
		return "Run operating system commands";
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
		
		// Run the operating system commands
		OperatingSystemRunCommandServiceInput operatingSystemCommandsInput = mapOperatingSystemCommandsInput(valuesDefiner);
		operatingSystemService.runCommands(operatingSystemCommandsInput);
		
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
		input.setRepositoryFolder(action.getFolder());
		input.setBranch(valuesDefiner.getValue(action.getGitCommit().getBranch()));
		input.setPull(action.getGitCommit().isPull());
		return input;
	}

	private OperatingSystemRunCommandServiceInput mapOperatingSystemCommandsInput(ValuesDefiner valuesDefiner) {
		
		OperatingSystemRunCommandServiceInput input = new OperatingSystemRunCommandServiceInput();
		input.setFolder(action.getFolder());
		input.setCommands(mapOperatingSystemCommandModels(valuesDefiner));
		return input;
	}

	private List<OperatingSystemCommandServiceModel> mapOperatingSystemCommandModels(ValuesDefiner valuesDefiner) {
		
		List<OperatingSystemCommandServiceModel> serviceCommands = new ArrayList<>();
		
		List<OperatingSystemCommand> commands = action.getCommands();

		for(OperatingSystemCommand command: commands) {
			
			String commandValue = valuesDefiner.getValue(command.getCommand());
			
			serviceCommands.add(mapOperatingSystemCommandModel(command, commandValue));
		}
		
		return serviceCommands;
	}

	private OperatingSystemCommandServiceModel mapOperatingSystemCommandModel(OperatingSystemCommand command, String commandValue) {
		
		OperatingSystemCommandServiceModel serviceCommand = new OperatingSystemCommandServiceModel();
		serviceCommand.setCommand(commandValue);
		serviceCommand.setSuppressOutput(command.isSuppressOutput());
		return serviceCommand;
	}

	private GitCommitChangesServiceInput mapGitCommitChangesInput(ValuesDefiner valuesDefiner, String originalBranch) {
		
		GitCommitChangesServiceInput input = new GitCommitChangesServiceInput();
		input.setRepositoryFolder(action.getFolder());
		input.setOriginalBranch(originalBranch);
		input.setMessage(valuesDefiner.getValue(action.getGitCommit().getMessage()));
		return input;
	}
}
