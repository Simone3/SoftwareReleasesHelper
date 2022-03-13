package com.utils.logic.git;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;

interface GitService {
	
	CloseableGit getGit(String repositoryPath);
	
	boolean isWorkingTreeClean(CloseableGit git);
	
	String getCurrentBranch(CloseableGit git);
	
	void checkBranchesExist(CloseableGit git, String... branches);
	
	Ref switchBranch(CloseableGit git, String branch);
	
	PullResult pull(CloseableGit git, String username, String password);
	
	MergeResult mergeIntoCurrentBranch(CloseableGit git, String sourceBranch, String message);
}
