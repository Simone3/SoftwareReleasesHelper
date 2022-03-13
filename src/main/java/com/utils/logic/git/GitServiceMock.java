package com.utils.logic.git;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class GitServiceMock implements GitService {

	private static final int ERRORS_WORKSPACE = 1;
	private static final int ERRORS_PULL = 1;
	private int errorsCounter = 0;
	
	@Override
	public CloseableGit getGit(String repositoryPath) {
		
		log.warn("Git operations disabled: skipping get repository with {}", repositoryPath);
		return null;
	}

	@Override
	public boolean isWorkingTreeClean(CloseableGit git) {
		
		log.warn("Git operations disabled: skipping check working tree");
		
		if(ERRORS_WORKSPACE > 0) {
			
			if(errorsCounter >= ERRORS_WORKSPACE) {
				
				errorsCounter = 0;
			}
			else {
				
				errorsCounter++;
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String getCurrentBranch(CloseableGit git) {
		
		log.warn("Git operations disabled: skipping get current branch");
		return "mock-branch";
	}

	@Override
	public void checkBranchesExist(CloseableGit git, String... branches) {
		
		log.warn("Git operations disabled: skipping check branches exist: {}", String.join(", ", branches));
		return;
	}

	@Override
	public Ref switchBranch(CloseableGit git, String branch) {

		log.warn("Git operations disabled: skipping branch switch to {}", branch);
		return null;
	}

	@Override
	public PullResult pull(CloseableGit git, String username, String password) {
		
		log.warn("Git operations disabled: skipping pull with username {}", username);
		
		if(ERRORS_PULL > 0) {
			
			if(errorsCounter >= ERRORS_PULL) {
				
				errorsCounter = 0;
			}
			else {
				
				errorsCounter++;
				throw new IllegalStateException("This is a mock Git pull error!");
			}
		}
		
		return null;
	}
	
	@Override
	public MergeResult mergeIntoCurrentBranch(CloseableGit git, String sourceBranch, String message) {
		
		log.warn("Git operations disabled: skipping merge from {} with message {}", sourceBranch, message);
		return new MergeResult(null, null, null, MergeStatus.MERGED, null, null, null);
	}
}
