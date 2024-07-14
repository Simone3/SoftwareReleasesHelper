package com.utils.releaseshelper.connector.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.error.GitUnauthorizedException;

/**
 * An implementation of the Git connector based on JGit
 */
public class GitConnectorReal implements GitConnector {
	
	private final int timeoutSeconds;

	public GitConnectorReal(GitConfig gitConfig) {
		
		timeoutSeconds = gitConfig.getTimeoutMilliseconds() / 1000;
	}

	@Override
	public GitRepository getRepository(String repositoryFolderPath) {

		return getRepository(new File(repositoryFolderPath));
	}

	@Override
	public GitRepository getRepository(File repositoryFolder) {
		
		File gitFolder = findGitRepository(repositoryFolder);
		
		Repository repository;
		try {
			
			repository = new FileRepositoryBuilder()
				.setGitDir(gitFolder)
				.setMustExist(true)
				.readEnvironment()
				.build();
		}
		catch(IOException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
		
		return new GitRepository(repository);
	}

	@Override
	public boolean isWorkingTreeClean(GitRepository gitRepository) {
		
		Status status;
		try {
			
			status = gitRepository.getHandler().status().call();
		}
		catch(NoWorkTreeException | GitAPIException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
		
		return status.isClean();
	}

	@Override
	public String getCurrentBranch(GitRepository gitRepository) {
		
		try {
			
			return gitRepository.getHandler().getRepository().getBranch();
		}
		catch(IOException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
	}

	@Override
	public Set<String> checkNonExistingBranches(GitRepository gitRepository, Collection<String> branchesToCheck) {
		
		if(CollectionUtils.isEmpty(branchesToCheck)) {
			
			throw new IllegalStateException("No branch to check was provided");
		}
		
		List<Ref> localBranches;
		try {
			
			localBranches = gitRepository.getHandler().branchList().call();
		}
		catch(GitAPIException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
		
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
	public Ref switchBranch(GitRepository gitRepository, String branch) {
		
		try {
			
			return gitRepository.getHandler().checkout().setName(branch).setCreateBranch(false).call();
		}
		catch(GitAPIException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
	}
	
	@Override
	public void addAll(GitRepository gitRepository) {
		
        try {
        	
        	// Stage all files in the repo including new files, excluding deleted files
			gitRepository.getHandler().add().addFilepattern(".").call();
			
			// Stage all changed files, including deleted files, excluding new files
	        gitRepository.getHandler().add().addFilepattern(".").setUpdate(true).call();
		}
        catch(GitAPIException e) {
			
        	throw new BusinessException("Error adding all", e);
		}
	}

	@Override
	public void commit(GitRepository gitRepository, String message) {
		
        try {
        	
			gitRepository
				.getHandler()
				.commit()
			    .setMessage(message)
			    .call();
		}
        catch(GitAPIException e) {
			
        	throw new BusinessException(e.getMessage(), e);
		}
	}
	
	@Override
	public PullResult pull(GitRepository gitRepository, String username, String password) {
		
		PullResult pullResult;
		try {
			
			pullResult = gitRepository
				.getHandler()
				.pull()
				.setTimeout(timeoutSeconds)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
				.call();
		}
		catch(TransportException e) {
			
			if(e.getMessage().contains("not authorized")) {
				
				throw new GitUnauthorizedException(e.getMessage(), e);
			}
			else {
				
				throw new BusinessException(e.getMessage(), e);
			}
		}
		catch(GitAPIException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
		
		if(!pullResult.isSuccessful()) {
			
			throw new BusinessException(pullResult.toString());
		}
		
		return pullResult;
	}

	@Override
	public MergeResult mergeIntoCurrentBranch(GitRepository gitRepository, String sourceBranch, String message) {

		try {
			
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
		catch(GitAPIException | RevisionSyntaxException | IOException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
	}
	
	private File findGitRepository(File folder) {
		
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
