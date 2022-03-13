package com.utils.logic.git;

import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;

import com.utils.logic.common.StepLogic;
import com.utils.model.git.GitData;
import com.utils.model.git.GitOperation;
import com.utils.model.git.GitStep;
import com.utils.model.main.Action;
import com.utils.model.properties.Properties;
import com.utils.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitLogic extends StepLogic<GitStep> {

	private static final String MESSAGE_PLACEHOLDER_SOURCE_BRANCH = "[SOURCE_BRANCH]";
	private static final String MESSAGE_PLACEHOLDER_TARGET_BRANCH = "[TARGET_BRANCH]";
	
	private final CommandLineInterface cli;
	private final GitService service;
	
	private final GitData gitData;
	private final boolean printPassword;
	
	private Action repository;

	public GitLogic(Properties properties, CommandLineInterface cli) {
		
		super(GitStep.DO_OPERATIONS, GitStep.EXIT);
		this.cli = cli;
		this.service = properties.isTestMode() ? new GitServiceMock() : new GitServiceReal(properties.getGit());
		this.gitData = properties.getGit();
		this.printPassword = properties.isPrintPasswords();
	}

	public void execute(Action action) {
		
		repository = action;
		loopSteps();
	}

	@Override
	protected GitStep processCurrentStep(GitStep currentStep) {
		
		switch(currentStep) {
		
			case DO_OPERATIONS:
				return doOperations();
				
			default:
				throw new IllegalStateException("Unknown step: " + currentStep);
		}
	}

	private GitStep doOperations() {
		
		String repositoryPath = StringUtils.isBlank(gitData.getBasePath()) ? repository.getGitRepositoryFolder() : Paths.get(gitData.getBasePath(), repository.getGitRepositoryFolder()).toString();
		String username = gitData.getUsername();
		String password = gitData.getPassword();
		
		cli.println("Manage repository with:");
		cli.println("  - Path: %s", repositoryPath);
		cli.println("  - Username: %s", username);
		cli.println("  - Password: %s", (printPassword ? password : "******"));
		cli.println("  - Operations:");
		var operations = repository.getGitOperations();
		for(var operation: operations) {
			
			cli.println("     - %s", operation);
		}
		
		cli.println();
		if(cli.askUserConfirmation("Start Git operations")) {
			
			cli.printSeparator();
			doOperations(repositoryPath, operations);
		}
		else {
			
			cli.println();
			cli.printSeparator(false);
		}
		
		clearState();
		return GitStep.EXIT;
	}
	
	private void doOperations(String repositoryFolder, List<GitOperation> operations) {
		
		try(CloseableGit git = service.getGit(repositoryFolder)) {

			while(!service.isWorkingTreeClean(git)) {
				
				cli.askUserConfirmation("Working tree is not clean, please commit or discard changed files. Done");
				cli.printSeparator();
			}
			
			cli.println("Working tree is clean");
			
			String originalBranch = service.getCurrentBranch(git);
			
			for(var operation: operations) {
				
				cli.println();
				cli.println("Start Git operation \"%s\"", operation);
				
				doOperation(git, operation);
				
				cli.println("End Git operation \"%s\"", operation);
			}

			service.switchBranch(git, originalBranch);
			
			cli.println();
			cli.println("Switched to %s (original branch)", originalBranch);
			
			cli.println();
			cli.println("All operations completed. Don't forget to push on all affected branches if needed!");
		}
	}
	
	private void doOperation(CloseableGit git, GitOperation operation) {
		
		switch(operation.getType()) {
		
			case MERGE:
				doMergeOperation(git, operation);
				return;
			
			default:
				throw new IllegalStateException("Unrecognized operation type: " + operation.getType());
		}
	}

	private void doMergeOperation(CloseableGit git, GitOperation operation) {

		boolean pull = operation.isPull();
		String sourceBranch = operation.getSourceBranch();
		String targetBranch = operation.getTargetBranch();
		
		String username = gitData.getUsername();
		String password = gitData.getPassword();
		String mergeMessage = gitData.getMergeMessage().replace(MESSAGE_PLACEHOLDER_SOURCE_BRANCH, sourceBranch).replace(MESSAGE_PLACEHOLDER_TARGET_BRANCH, targetBranch);
		
		service.checkBranchesExist(git, sourceBranch, targetBranch);
		
		if(pull) {
			
			service.switchBranch(git, sourceBranch);
			cli.println("Switched to %s (source branch)", sourceBranch);
			
			pullWithRetries(git, sourceBranch, username, password);
		}
		
		service.switchBranch(git, targetBranch);
		cli.println("Switched to %s (target branch)", targetBranch);

		if(pull) {
			
			pullWithRetries(git, targetBranch, username, password);
		}
		
		MergeResult mergeResult = service.mergeIntoCurrentBranch(git, sourceBranch, mergeMessage);
		MergeStatus mergeStatus = mergeResult.getMergeStatus();
		if(mergeStatus.isSuccessful()) {
			
			cli.println("Successfully merged %s into %s with status: %s", sourceBranch, targetBranch, mergeStatus);
		}
		else {

			cli.printError("Error merging %s into %s, status is: %s", sourceBranch, targetBranch, mergeStatus);

			do {
				
				cli.askUserConfirmation("Please resolve the merge manually and commit, leaving the working tree clean. Done");
			}
			while(!service.isWorkingTreeClean(git));
			
			cli.printSeparator();
		}
	}
	
	private boolean pullWithRetries(CloseableGit git, String currentBranch, String username, String password) {
		
		boolean first = true;
		
		while(first || cli.askUserConfirmation("Retry pulling from \"%s\"", currentBranch)) {
			
			if(first) {
				
				first = false;
			}
			else {
				
				cli.printSeparator();
			}
			
			try {
				
				cli.println("Start pulling from %s...", currentBranch);
				
				service.pull(git, username, password);
				
				cli.println("Successfully pulled from %s", currentBranch);
				
				return true;
			}
			catch(Exception e) {
				
				cli.printError("Cannot pull from \"%s\": %s", currentBranch, e.getMessage());
				log.error("Error pulling", e);
			}
		}
		
		cli.println();
		cli.printSeparator(false);
		
		return false;
	}
	
	private void clearState() {
		
		this.repository = null;
	}
}
