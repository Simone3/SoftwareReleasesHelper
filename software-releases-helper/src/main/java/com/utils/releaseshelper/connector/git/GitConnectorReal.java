package com.utils.releaseshelper.connector.git;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.error.BusinessException;

import lombok.SneakyThrows;

/**
 * An implementation of the Git connector based on JGit
 */
public class GitConnectorReal implements GitConnector {
	
	private final int timeoutSeconds;

	public GitConnectorReal(GitConfig gitConfig) {
		
		timeoutSeconds = gitConfig.getTimeoutMilliseconds() / 1000;
	}

	@Override
	@SneakyThrows
	public GitRepository getRepository(String repositoryFolder) {
		
		File gitFolder = findGitRepository(repositoryFolder);
		
		Repository repository = new FileRepositoryBuilder()
			.setGitDir(gitFolder)
			.setMustExist(true)
			.readEnvironment()
			.build();
		
		return new GitRepository(repository);
	}

	@Override
	@SneakyThrows
	public boolean isWorkingTreeClean(GitRepository gitRepository) {
		
		Status status = gitRepository.getHandler().status().call();
		return status.isClean();
	}

	@Override
	@SneakyThrows
	public String getCurrentBranch(GitRepository gitRepository) {
		
		return gitRepository.getHandler().getRepository().getBranch();
	}

	@Override
	@SneakyThrows
	public Set<String> checkNonExistingBranches(GitRepository gitRepository, Collection<String> branchesToCheck) {
		
		if(CollectionUtils.isEmpty(branchesToCheck)) {
			
			throw new IllegalStateException("No branch to check was provided");
		}
		
		List<Ref> localBranches = gitRepository.getHandler().branchList().call();
		Set<String> existingBranchesSet = new HashSet<>();
		for(Ref localBranch: localBranches) {
			
			existingBranchesSet.add(Repository.shortenRefName(localBranch.getName()));
		}
		
		Set<String> nonExistingBranches = new HashSet<>();
	
		for(String branch: branchesToCheck) {
			
			if(!existingBranchesSet.contains(branch)) {
				
				nonExistingBranches.add(branch);
			}
		}
		
		return nonExistingBranches;
	}

	@Override
	@SneakyThrows
	public Ref switchBranch(GitRepository gitRepository, String branch) {
		
		return gitRepository.getHandler().checkout().setName(branch).setCreateBranch(false).call();
	}
	
	@Override
	@SneakyThrows
	public void addAll(GitRepository gitRepository) {
		
		// Stage all files in the repo including new files, excluding deleted files
        gitRepository.getHandler().add().addFilepattern(".").call();

        // Stage all changed files, including deleted files, excluding new files
        gitRepository.getHandler().add().addFilepattern(".").setUpdate(true).call();
	}

	@Override
	@SneakyThrows
	public void commit(GitRepository gitRepository, String message) {
		
        gitRepository
        	.getHandler()
        	.commit()
            .setMessage(message)
            .call();
	}
	
	@Override
	@SneakyThrows
	public PullResult pull(GitRepository gitRepository, String username, String password) {
		
		PullResult pullResult = gitRepository
			.getHandler()
			.pull()
			.setTimeout(timeoutSeconds)
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
			.call();
		
		if(!pullResult.isSuccessful()) {
			
			throw new BusinessException(pullResult.toString());
		}
		
		return pullResult;
	}

	@Override
	@SneakyThrows
	public MergeResult mergeIntoCurrentBranch(GitRepository gitRepository, String sourceBranch, String message) {
		
		ObjectId mergeSource = gitRepository.getHandler().getRepository().resolve(sourceBranch);
		
		return gitRepository
			.getHandler()
			.merge()
            .include(mergeSource)
            .setCommit(true)
            .setFastForward(MergeCommand.FastForwardMode.NO_FF)
            .setSquash(false)
            .setMessage(message)
            .call();
	}
	
	private File findGitRepository(String folderPath) {
		
		File folder = new File(folderPath);
		String absoluteFolderPath = folder.getAbsolutePath();
		
		if(!folder.exists()) {
			
			throw new BusinessException("Folder " + absoluteFolderPath + " does not exist!");
		}
		
		if(!folder.isDirectory()) {
			
			throw new BusinessException(absoluteFolderPath + " is not a folder!");
		}
		
		while(folder != null) {
			
			File gitFolder = new File(folder.getAbsolutePath() + File.separator + ".git");

			if(gitFolder.exists() && gitFolder.isDirectory()) {
				
				return gitFolder;
			}
			
			folder = folder.getParentFile();
		}
		
		throw new BusinessException("Folder " + absoluteFolderPath + " is not a Git repository nor is part of one");
	}
}
