package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;

import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.logic.git.GitMerge;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitMergeProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates Git properties
 */
@UtilityClass
public class GitMapperValidator {

	public static GitConfig mapAndValidateGitConfig(GitProperties gitProperties) {
		
		ValidationUtils.notNull(gitProperties, "No Git properties are defined");
		
		String username = ValidationUtils.notBlank(gitProperties.getUsername(), "Git username is empty");
		String password = ValidationUtils.notBlank(gitProperties.getPassword(), "Git password is empty");
		String mergeMessage = ValidationUtils.notBlank(gitProperties.getMergeMessage(), "Git merge message is empty");
		Integer timeoutMilliseconds = ValidationUtils.positive(gitProperties.getTimeoutMilliseconds(), "Git timeout is empty or invalid");
		
		GitConfig gitConfig = new GitConfig();
		gitConfig.setUsername(username);
		gitConfig.setPassword(password);
		gitConfig.setMergeMessage(mergeMessage);
		gitConfig.setTimeoutMilliseconds(timeoutMilliseconds);
		return gitConfig;
	}

	public static List<GitMerge> mapAndValidateGitMerges(List<GitMergeProperty> mergesProperties) {
		
		ValidationUtils.notEmpty(mergesProperties, "At least one merge should be defined");
		
		List<GitMerge> merges = new ArrayList<>();
		
		for(int i = 0; i < mergesProperties.size(); i++) {
			
			GitMergeProperty mergeProperty = mergesProperties.get(i);
			
			try {
				
				merges.add(mapAndValidateGitMerge(mergeProperty));
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid merge at index " + i + " -> " + e.getMessage());
			}
		}
		
		return merges;
	}
	
	public static GitMerge mapAndValidateGitMerge(GitMergeProperty mergeProperty) {
		
		Boolean pull = mergeProperty.getPull();
		String sourceBranch = ValidationUtils.notBlank(mergeProperty.getSourceBranch(), "Merge does not have a source branch");
		String targetBranch = ValidationUtils.notBlank(mergeProperty.getTargetBranch(), "Merge does not have a target branch");
		
		if(sourceBranch.trim().equals(targetBranch.trim())) {
		
			throw new ValidationException("Merge has the same source and target branches");
		}
		
		GitMerge merge = new GitMerge();
		merge.setPull(pull != null && pull);
		merge.setSourceBranch(sourceBranch);
		merge.setTargetBranch(targetBranch);
		return merge;
	}
	
	public static GitCommit mapAndValidateGitCommit(GitCommitProperty commitProperty) {
		
		Boolean pull = commitProperty.getPull();
		String branch = ValidationUtils.notBlank(commitProperty.getBranch(), "Commit does not have a branch");
		String message = ValidationUtils.notBlank(commitProperty.getCommitMessage(), "Commit does not have a message");
		
		GitCommit commit = new GitCommit();
		commit.setPull(pull != null && pull);
		commit.setBranch(branch);
		commit.setMessage(message);
		return commit;
	}
}
