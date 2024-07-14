package com.utils.releaseshelper.connector.git;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;

import com.utils.releaseshelper.model.error.MockException;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Git connector, for test purposes
 */
@Slf4j
public class GitConnectorMock implements GitConnector {

	private int workspaceErrorsProbability = 0;
	private int pullErrorsProbability = 0;
	
	private long pullDelay = 2000l;
	
	@Override
	public GitRepository getRepository(String repositoryPath) {
		
		log.warn("Git operations disabled: skipping get repository with {}", repositoryPath);
		return null;
	}

	@Override
	public GitRepository getRepository(File repositoryFolder) {
		
		log.warn("Git operations disabled: skipping get repository with {}", repositoryFolder);
		return null;
	}
	
	@Override
	public boolean isWorkingTreeClean(GitRepository gitRepository) {
		
		log.warn("Git operations disabled: skipping check working tree");
		
		return !(workspaceErrorsProbability > 0 && ThreadLocalRandom.current().nextInt(1, 101) <= workspaceErrorsProbability);
	}

	@Override
	public String getCurrentBranch(GitRepository gitRepository) {
		
		log.warn("Git operations disabled: skipping get current branch");
		return "mock-branch";
	}

	@Override
	public Set<String> checkNonExistingBranches(GitRepository gitRepository, Collection<String> branchesToCheck) {
		
		log.warn("Git operations disabled: skipping check branches exist: {}", String.join(", ", branchesToCheck));
		return new HashSet<>();
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
		
		if(pullDelay > 0) {
			
			try {
				
				Thread.sleep(pullDelay);
			}
			catch(InterruptedException e) {
				
				Thread.currentThread().interrupt();
			}
		}
		
		if(pullErrorsProbability > 0 && ThreadLocalRandom.current().nextInt(1, 101) <= pullErrorsProbability) {
			
			throw new MockException("This is a mock Git pull error!");
		}
		
		return null;
	}
	
	@Override
	public MergeResult mergeIntoCurrentBranch(GitRepository gitRepository, String sourceBranch, String message) {
		
		log.warn("Git operations disabled: skipping merge from {} with message {}", sourceBranch, message);
		return new MergeResult(null, null, null, MergeStatus.MERGED, null, null, null);
	}
}
