package com.utils.releaseshelper.connector.git;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Git connector, for test purposes
 */
@Slf4j
public class GitConnectorMock implements GitConnector {

	private int workspaceErrorsToThrow = 0;
	private int pullErrorsToThrow = 0;
	private int thrownErrors = 0;
	
	@Override
	public GitRepository getRepository(String repositoryPath) {
		
		log.warn("Git operations disabled: skipping get repository with {}", repositoryPath);
		return null;
	}

	@Override
	public boolean isWorkingTreeClean(GitRepository gitRepository) {
		
		log.warn("Git operations disabled: skipping check working tree");
		
		if(workspaceErrorsToThrow > 0) {
			
			if(thrownErrors >= workspaceErrorsToThrow) {
				
				thrownErrors = 0;
			}
			else {
				
				thrownErrors++;
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String getCurrentBranch(GitRepository gitRepository) {
		
		log.warn("Git operations disabled: skipping get current branch");
		return "mock-branch";
	}

	@Override
	public void checkBranchesExist(GitRepository gitRepository, String... branches) {
		
		log.warn("Git operations disabled: skipping check branches exist: {}", String.join(", ", branches));
		return;
	}

	@Override
	public Ref switchBranch(GitRepository gitRepository, String branch) {

		log.warn("Git operations disabled: skipping branch switch to {}", branch);
		return null;
	}

	@Override
	public void addAll(GitRepository gitRepository) {
		
		log.warn("Git operations disabled: skipping add all");
	}

	@Override
	public void commit(GitRepository gitRepository, String message) {
		
		log.warn("Git operations disabled: skipping commit with message {}", message);
	}
	
	@Override
	public PullResult pull(GitRepository gitRepository, String username, String password) {
		
		log.warn("Git operations disabled: skipping pull with username {}", username);
		
		if(pullErrorsToThrow > 0) {
			
			if(thrownErrors >= pullErrorsToThrow) {
				
				thrownErrors = 0;
			}
			else {
				
				thrownErrors++;
				throw new IllegalStateException("This is a mock Git pull error!");
			}
		}
		
		return null;
	}
	
	@Override
	public MergeResult mergeIntoCurrentBranch(GitRepository gitRepository, String sourceBranch, String message) {
		
		log.warn("Git operations disabled: skipping merge from {} with message {}", sourceBranch, message);
		return new MergeResult(null, null, null, MergeStatus.MERGED, null, null, null);
	}
}
