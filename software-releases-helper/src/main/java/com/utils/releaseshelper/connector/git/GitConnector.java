package com.utils.releaseshelper.connector.git;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;

import com.utils.releaseshelper.connector.Connector;

/**
 * The connector to interact with a Git repository
 */
public interface GitConnector extends Connector {
	
	GitRepository getRepository(String repositoryPath);
	
	GitRepository getRepository(File repositoryFolder);

	boolean isWorkingTreeClean(GitRepository gitRepository);
	
	String getCurrentBranch(GitRepository gitRepository);

	Set<String> checkNonExistingBranches(GitRepository gitRepository, Collection<String> branches);
	
	Ref switchBranch(GitRepository gitRepository, String branch);
	
	void addAll(GitRepository gitRepository);
	
	void commit(GitRepository gitRepository, String message);

	PullResult pull(GitRepository gitRepository, String username, String password);

	MergeResult mergeIntoCurrentBranch(GitRepository gitRepository, String sourceBranch, String message);
}
