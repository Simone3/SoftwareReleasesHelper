package com.utils.releaseshelper.service.git;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;

import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitConnectorMock;
import com.utils.releaseshelper.connector.git.GitConnectorReal;
import com.utils.releaseshelper.connector.git.GitRepository;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.git.GitLocalBranchErrorRemediation;
import com.utils.releaseshelper.model.logic.git.GitPullErrorRemediation;
import com.utils.releaseshelper.model.logic.git.GitWorkingTreeErrorRemediation;
import com.utils.releaseshelper.model.service.git.GitCommitChangesServiceInput;
import com.utils.releaseshelper.model.service.git.GitMergeServiceModel;
import com.utils.releaseshelper.model.service.git.GitMergesServiceInput;
import com.utils.releaseshelper.model.service.git.GitPrepareForChangesServiceInput;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.utils.ErrorRemediationUtils;
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
			
			checkBranchesExistWithErrorRemediation(git, branch);
			
			connector.switchBranch(git, branch);
			cli.println("Switched to %s", branch);
			
			if(pull) {
				
				pullWithErrorRemediation(git, gitConfig, branch);
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
			
			boolean actuallyCommitted = commitAll(git, branch, message);
			
			connector.switchBranch(git, originalBranch);
			
			cli.println();
			cli.println("Switched to %s (original branch)", originalBranch);
			
			if(actuallyCommitted) {
				
				cli.println();
				cli.println("Commit changes completed. Don't forget to manually push the target branch!");
			}
		}
	}

	public void merge(GitMergesServiceInput mergesInput) {
		
		String repositoryFolder = mergesInput.getRepositoryFolder();
		List<GitMergeServiceModel> merges = mergesInput.getMerges();

		try(GitRepository git = connector.getRepository(repositoryFolder)) {
			
			String originalBranch = connector.getCurrentBranch(git);
			
			checkAndMerge(git, merges);
			
			connector.switchBranch(git, originalBranch);
			
			cli.println();
			cli.println("Switched to %s (original branch)", originalBranch);
		}
		
		cli.println();
		cli.println(merges.size() == 1 ? "Merge completed. Don't forget to manually push the target branch!" : "All merges completed. Don't forget to manually push the target branches!");
	}
	
	private void checkWorkingTreeClean(GitRepository git) {
		
		checkWorkingTreeClean(git, "Error checking working tree", "Working tree is not clean, please commit or discard changed files");
	}

	private void checkWorkingTreeClean(GitRepository git, String errorMessage, String exceptionMessage) {
		
		ErrorRemediationUtils.runWithErrorRemediation(
			cli,
			errorMessage,
			GitWorkingTreeErrorRemediation.values(),
			() -> {
				
				if(!connector.isWorkingTreeClean(git)) {
					
					throw new BusinessException(exceptionMessage);
				}
			}
		);
		
		cli.println("Working tree is clean");
	}

	private void checkAndMerge(GitRepository git, List<GitMergeServiceModel> merges) {
		
		Set<String> pulledBranches = new HashSet<>();
		
		for(int i = 0; i < merges.size(); i++) {
			
			if(i != 0) {
				
				cli.println();
			}
			
			checkWorkingTreeClean(git);
			
			doActualMerge(git, merges.get(i), pulledBranches);
		}
	}
	
	private void doActualMerge(GitRepository git, GitMergeServiceModel merge, Set<String> pulledBranches) {
		
		boolean pull = merge.isPull();
		String sourceBranch = merge.getSourceBranch();
		String targetBranch = merge.getTargetBranch();
		
		Map<String, String> mergeMessagePlaceholders = new HashMap<>();
		mergeMessagePlaceholders.put(MESSAGE_PLACEHOLDER_SOURCE_BRANCH, sourceBranch);
		mergeMessagePlaceholders.put(MESSAGE_PLACEHOLDER_TARGET_BRANCH, targetBranch);
		String mergeMessage = VariablesUtils.replaceVariablePlaceholders(gitConfig.getMergeMessage(), null, mergeMessagePlaceholders);
		
		checkBranchesExistWithErrorRemediation(git, sourceBranch, targetBranch);
		
		if(pull && !pulledBranches.contains(sourceBranch)) {
			
			connector.switchBranch(git, sourceBranch);
			cli.println("Switched to %s (source branch)", sourceBranch);
			
			pulledBranches.add(sourceBranch);
			pullWithErrorRemediation(git, gitConfig, sourceBranch);
		}
		
		connector.switchBranch(git, targetBranch);
		cli.println("Switched to %s (target branch)", targetBranch);

		if(pull && !pulledBranches.contains(targetBranch)) {

			pulledBranches.add(targetBranch);
			pullWithErrorRemediation(git, gitConfig, targetBranch);
		}
		
		MergeResult mergeResult = connector.mergeIntoCurrentBranch(git, sourceBranch, mergeMessage);
		MergeStatus mergeStatus = mergeResult.getMergeStatus();
		if(mergeStatus.isSuccessful()) {
			
			cli.println("Successfully merged %s into %s with status: %s", sourceBranch, targetBranch, mergeStatus);
		}
		else {

			cli.printError("Error merging %s into %s, status is %s", sourceBranch, targetBranch, mergeStatus);

			checkWorkingTreeClean(git, "Unfinished merge", "Please resolve the merge manually and commit, leaving the working tree clean");
		}
	}

	private boolean commitAll(GitRepository git, String currentBranch, String message) {
		
		if(connector.isWorkingTreeClean(git)) {
			
			cli.println("Nothing to commit");
			
			return false;
		}
		else {
			
		    connector.addAll(git);
		    connector.commit(git, message);
		    
		    cli.println("Successfully committed all changes on %s with comment \"%s\"", currentBranch, message);
		    
		    return true;
		}
	}
	
	private boolean checkBranchesExistWithErrorRemediation(GitRepository git, String... branches) {
		
		return ErrorRemediationUtils.runWithErrorRemediation(
			cli,
			"Error checking branches",
			GitLocalBranchErrorRemediation.values(),
			() -> connector.checkBranchesExist(git, branches)
		);
	}
	
	private boolean pullWithErrorRemediation(GitRepository git, GitConfig gitConfig, String currentBranch) {
		
		String username = gitConfig.getUsername();
		String password = gitConfig.getPassword();
		
		return ErrorRemediationUtils.runWithErrorRemediation(
			cli,
			"Error pulling from " + currentBranch,
			GitPullErrorRemediation.values(),
			() -> {
				
				cli.println("Start pulling from %s...", currentBranch);
				
				connector.pull(git, username, password);
				
				cli.println("Successfully pulled from %s", currentBranch);
			}
		);
	}
}
