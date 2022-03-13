package com.utils.logic.git;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.utils.model.git.GitData;

import lombok.SneakyThrows;

class GitServiceReal implements GitService {
	
	private final int timeoutSeconds;

	public GitServiceReal(GitData gitData) {
		
		timeoutSeconds = gitData.getTimeoutMilliseconds() / 1000;
	}

	@Override
	@SneakyThrows
	public CloseableGit getGit(String repositoryPath) {
		
		File gitFolder = new File(Paths.get(repositoryPath, ".git").toString());
		
		if(!gitFolder.exists()) {
			
			throw new IllegalStateException("Folder " + gitFolder.getAbsolutePath() + " does not exist!");
		}
		
		if(!gitFolder.isDirectory()) {
			
			throw new IllegalStateException(gitFolder.getAbsolutePath() + " is not a folder!");
		}
		
		Repository repository = new FileRepositoryBuilder()
			.setGitDir(gitFolder)
			.setMustExist(true)
			.readEnvironment()
			.build();
		
		return new CloseableGit(repository);
	}

	@Override
	@SneakyThrows
	public boolean isWorkingTreeClean(CloseableGit git) {
		
		Status status = git.status().call();
		return status.isClean();
	}

	@Override
	@SneakyThrows
	public String getCurrentBranch(CloseableGit git) {
		
		return git.getRepository().getBranch();
	}

	@Override
	@SneakyThrows
	public void checkBranchesExist(CloseableGit git, String... branches) {
		
		if(branches == null || branches.length == 0) {
			
			throw new IllegalStateException("No branch to check was provided");
		}
		
		var localBranches = git.branchList().call();
		Map<String, Void> branchesMap = new HashMap<>();
		for(var localBranch: localBranches) {
			
			branchesMap.put(Repository.shortenRefName(localBranch.getName()), null);
		}
	
		for(var branch: branches) {
			
			if(!branchesMap.containsKey(branch)) {
				
				throw new IllegalStateException("Branch " + branch + " does not exist in local branches");
			}
		}
	}

	@Override
	@SneakyThrows
	public Ref switchBranch(CloseableGit git, String branch) {
		
		return git.checkout().setName(branch).setCreateBranch(false).call();
	}

	@Override
	@SneakyThrows
	public PullResult pull(CloseableGit git, String username, String password) {
		
		PullResult pullResult = git
			.pull()
			.setTimeout(timeoutSeconds)
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
			.call();
		
		if(!pullResult.isSuccessful()) {
			
			throw new IllegalStateException(pullResult.toString());
		}
		
		return pullResult;
	}

	@Override
	@SneakyThrows
	public MergeResult mergeIntoCurrentBranch(CloseableGit git, String sourceBranch, String message) {
		
		ObjectId mergeSource = git.getRepository().resolve(sourceBranch);
		
		return git
			.merge()
            .include(mergeSource)
            .setCommit(true)
            .setFastForward(MergeCommand.FastForwardMode.NO_FF)
            .setSquash(false)
            .setMessage(message)
            .call();
	}
}
