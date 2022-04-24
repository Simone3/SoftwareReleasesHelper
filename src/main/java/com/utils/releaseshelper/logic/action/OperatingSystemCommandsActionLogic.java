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
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more generic operating system commands, optionally committing all changes (if any) to a Git repository
 */
public class OperatingSystemCommandsActionLogic extends ActionLogic<OperatingSystemCommandsAction> {

	private final OperatingSystemService operatingSystemService;
	private final GitService gitService;

	private List<String> commandValues = new ArrayList<>();
	private String commitBranch;
	private String commitMessage;

	protected OperatingSystemCommandsActionLogic(OperatingSystemCommandsAction action, Map<String, String> variables, CommandLineInterface cli, OperatingSystemService operatingSystemService, GitService gitService) {
		
		super(action, variables, cli);
		this.operatingSystemService = operatingSystemService;
		this.gitService = gitService;
	}

	@Override
	protected void beforeAction() {
		
		defineCommandValues();
		defineCommit();
	}

	@Override
	protected void printActionDescription() {
		
		String folder = action.getFolder();
		List<OperatingSystemCommand> commands = action.getCommands();
		
		cli.println("Run these commands in %s:", folder);
		
		for(int i = 0; i < commands.size(); i++) {
			
			String commandValue = commandValues.get(i);
			cli.println("  - %s", commandValue);
		}

		if(action.getGitCommit() != null) {
			
			cli.println("And then commit all changes:");
			cli.println("  - Branch: %s", commitBranch);
			cli.println("  - Message: %s", commitMessage);
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Run operating system commands";
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
		
		// Run the operating system commands
		OperatingSystemRunCommandServiceInput operatingSystemCommandsInput = mapOperatingSystemCommandsInput();
		operatingSystemService.runCommands(operatingSystemCommandsInput);
		
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

	private void defineCommandValues() {
		
		List<OperatingSystemCommand> commands = action.getCommands();
		for(int i = 0; i < commands.size(); i++) {
			
			OperatingSystemCommand command = commands.get(i);
			commandValues.add(VariablesUtils.defineValue(cli, "Define command at index " + i, command.getCommand(), variables));
		}
	}
	
	private void defineCommit() {
		
		GitCommit gitCommit = action.getGitCommit();
		if(gitCommit != null) {
			
			commitBranch = VariablesUtils.defineValue(cli, "Define Git commit branch", gitCommit.getBranch(), variables);
			commitMessage = VariablesUtils.defineValue(cli, "Define Git commit message", gitCommit.getMessage(), variables);
		}
	}
	
	private GitPrepareForChangesServiceInput mapGitPrepareForChangesInput() {
		
		GitPrepareForChangesServiceInput input = new GitPrepareForChangesServiceInput();
		input.setRepositoryFolder(action.getFolder());
		input.setBranch(commitBranch);
		input.setPull(action.getGitCommit().isPull());
		return input;
	}

	private OperatingSystemRunCommandServiceInput mapOperatingSystemCommandsInput() {
		
		OperatingSystemRunCommandServiceInput input = new OperatingSystemRunCommandServiceInput();
		input.setFolder(action.getFolder());
		input.setCommands(mapOperatingSystemCommandModels());
		return input;
	}

	private List<OperatingSystemCommandServiceModel> mapOperatingSystemCommandModels() {
		
		List<OperatingSystemCommandServiceModel> serviceCommands = new ArrayList<>();
		
		List<OperatingSystemCommand> commands = action.getCommands();

		for(int i = 0; i < commands.size(); i++) {
			
			OperatingSystemCommand command = commands.get(i);
			String commandValue = commandValues.get(i);
			
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

	private GitCommitChangesServiceInput mapGitCommitChangesInput(String originalBranch) {
		
		GitCommitChangesServiceInput input = new GitCommitChangesServiceInput();
		input.setRepositoryFolder(action.getFolder());
		input.setOriginalBranch(originalBranch);
		input.setMessage(commitMessage);
		return input;
	}
}
