package com.utils.releaseshelper.service.git;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;

import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitConnectorMock;
import com.utils.releaseshelper.connector.git.GitConnectorReal;
import com.utils.releaseshelper.connector.git.GitRepository;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.service.git.GitCommitChangesServiceInput;
import com.utils.releaseshelper.model.service.git.GitMergeServiceModel;
import com.utils.releaseshelper.model.service.git.GitMergesServiceInput;
import com.utils.releaseshelper.model.service.git.GitPrepareForChangesServiceInput;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.utils.RetryUtils;
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * A Service that allows to operate on a Git repository
 */
public class GitService implements Service {

	private static final String MESSAGE_PLACEHOLDER_SOURCE_BRANCH = "SOURCE_BRANCH";
	private static final String MESSAGE_PLACEHOLDER_TARGET_BRANCH = "TARGET_BRANCH";
	
	private final CommandLineInterface cli;
	private final GitConnector connector;
	private final GitConfig gitConfig;

	public GitService(Config config, CommandLineInterface cli) {
		
		this.cli = cli;
		this.connector = config.isTestMode() ? new GitConnectorMock() : new GitConnectorReal(config.getGit());
		this.gitConfig = config.getGit();
	}
	
	public String prepareForChanges(GitPrepareForChangesServiceInput input) {
		
		String repositoryFolder = input.getRepositoryFolder();
		String branch = input.getBranch();
		boolean pull = input.isPull();
		
		try(GitRepository git = connector.getRepository(repositoryFolder)) {
			
			checkWorkingTreeClean(git);
			
			String originalBranch = connector.getCurrentBranch(git);
			
			checkBranchesExistWithRetries(git, branch);
			
			connector.switchBranch(git, branch);
			cli.println("Switched to %s", branch);
			
			if(pull) {
				
				pullWithRetries(git, gitConfig, branch);
			}
			
			return originalBranch;
		}
	}
	
	public void commitChanges(GitCommitChangesServiceInput input) {
		
		String repositoryFolder = input.getRepositoryFolder();
		String originalBranch = input.getOriginalBranch();
		String message = input.getMessage();
		
		try(GitRepository git = connector.getRepository(repositoryFolder)) {
			
			String branch = connector.getCurrentBranch(git);
			
			commitAll(git, branch, message);
			
			connector.switchBranch(git, originalBranch);
			
			cli.println();
			cli.println("Switched to %s (original branch)", originalBranch);
			
			cli.println();
			cli.println("Commit changes completed. Don't forget to manually push the target branch!");
		}
	}

	public void merges(GitMergesServiceInput mergesInput) {
		
		String repositoryFolder = mergesInput.getRepositoryFolder();
		List<GitMergeServiceModel> merges = mergesInput.getMerges();

		try(GitRepository git = connector.getRepository(repositoryFolder)) {
			
			for(int i = 0; i < merges.size(); i++) {
				
				if(i != 0) {
					
					cli.println();
				}
				
				prepareAndMerge(git, merges.get(i));
			}
		}
	}

	private void checkWorkingTreeClean(GitRepository git) {
		
		while(!connector.isWorkingTreeClean(git)) {
			
			cli.askUserConfirmation("Working tree is not clean, please commit or discard changed files. Done");
		}
		
		cli.println("Working tree is clean");
	}
	
	private void prepareAndMerge(GitRepository git, GitMergeServiceModel merge) {
		
		checkWorkingTreeClean(git);
		
		String originalBranch = connector.getCurrentBranch(git);
		
		doActualMerge(git, merge);
		
		connector.switchBranch(git, originalBranch);
		
		cli.println();
		cli.println("Switched to %s (original branch)", originalBranch);
		
		cli.println();
		cli.println("Merge completed. Don't forget to manually push the target branch!");
	}
	
	private void doActualMerge(GitRepository git, GitMergeServiceModel merge) {
		
		boolean pull = merge.isPull();
		String sourceBranch = merge.getSourceBranch();
		String targetBranch = merge.getTargetBranch();
		
		Map<String, String> mergeMessagePlaceholders = new HashMap<>();
		mergeMessagePlaceholders.put(MESSAGE_PLACEHOLDER_SOURCE_BRANCH, sourceBranch);
		mergeMessagePlaceholders.put(MESSAGE_PLACEHOLDER_TARGET_BRANCH, targetBranch);
		String mergeMessage = VariablesUtils.replaceVariablePlaceholders(gitConfig.getMergeMessage(), null, mergeMessagePlaceholders);
		
		checkBranchesExistWithRetries(git, sourceBranch, targetBranch);
		
		if(pull) {
			
			connector.switchBranch(git, sourceBranch);
			cli.println("Switched to %s (source branch)", sourceBranch);
			
			pullWithRetries(git, gitConfig, sourceBranch);
		}
		
		connector.switchBranch(git, targetBranch);
		cli.println("Switched to %s (target branch)", targetBranch);

		if(pull) {
			
			pullWithRetries(git, gitConfig, targetBranch);
		}
		
		MergeResult mergeResult = connector.mergeIntoCurrentBranch(git, sourceBranch, mergeMessage);
		MergeStatus mergeStatus = mergeResult.getMergeStatus();
		if(mergeStatus.isSuccessful()) {
			
			cli.println("Successfully merged %s into %s with status: %s", sourceBranch, targetBranch, mergeStatus);
		}
		else {

			cli.printError("Error merging %s into %s, status is: %s", sourceBranch, targetBranch, mergeStatus);

			do {
				
				cli.askUserConfirmation("Please resolve the merge manually and commit, leaving the working tree clean. Done");
			}
			while(!connector.isWorkingTreeClean(git));
			
			cli.println("Working tree is clean");
		}
	}

	private void commitAll(GitRepository git, String currentBranch, String message) {
		
		if(connector.isWorkingTreeClean(git)) {
			
			cli.println("Nothing to commit");
		}
		else {
			
		    connector.addAll(git);
		    connector.commit(git, message);
		    
		    cli.println("Successfully committed all changes on %s with comment \"%s\"", currentBranch, message);
		}
	}
	
	private boolean checkBranchesExistWithRetries(GitRepository git, String... branches) {
		
		boolean branchesExist = RetryUtils.retry(cli, "Retry checking local branches", "Error checking branches", () -> connector.checkBranchesExist(git, branches));
		
		if(!branchesExist) {
			
			throw new IllegalStateException("Cannot run Git if branches do not exist");
		}
		
		return branchesExist;
	}
	
	private boolean pullWithRetries(GitRepository git, GitConfig gitConfig, String currentBranch) {
		
		String username = gitConfig.getUsername();
		String password = gitConfig.getPassword();
		
		boolean pullSuccess = RetryUtils.retry(cli, "Retry pulling from \"" + currentBranch + "\"", "Cannot pull from \"" + currentBranch + "\"", () -> {
			
			cli.println("Start pulling from %s...", currentBranch);
			
			connector.pull(git, username, password);
			
			cli.println("Successfully pulled from %s", currentBranch);
		});
		
		if(!pullSuccess) {
			
			cli.println("Pull skipped");
		}
		
		return pullSuccess;
	}
}
