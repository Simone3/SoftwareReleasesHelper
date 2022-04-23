package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.List;

import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.logic.ValueDefinition;
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

		String sourceBranchProperty = mergeProperty.getSourceBranch();
		String targetBranchProperty = mergeProperty.getTargetBranch();
		Boolean pull = mergeProperty.getPull();
		
		ValueDefinition sourceBranch;
		try {
			
			sourceBranch = VariablesMapperValidator.mapAndValidateValueDefinition(sourceBranchProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid merge source branch -> " + e.getMessage());
		}
		
		ValueDefinition targetBranch;
		try {
			
			targetBranch = VariablesMapperValidator.mapAndValidateValueDefinition(targetBranchProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid merge target branch -> " + e.getMessage());
		}
		
		GitMerge merge = new GitMerge();
		merge.setPull(pull != null && pull);
		merge.setSourceBranch(sourceBranch);
		merge.setTargetBranch(targetBranch);
		return merge;
	}
	
	public static GitCommit mapAndValidateGitCommit(GitCommitProperty commitProperty) {

		String branchProperty = commitProperty.getBranch();
		String messageProperty = commitProperty.getCommitMessage();
		Boolean pull = commitProperty.getPull();
		
		ValueDefinition branch;
		try {
			
			branch = VariablesMapperValidator.mapAndValidateValueDefinition(branchProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid commit branch -> " + e.getMessage());
		}
		
		ValueDefinition message;
		try {
			
			message = VariablesMapperValidator.mapAndValidateValueDefinition(messageProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid commit message -> " + e.getMessage());
		}
		
		GitCommit commit = new GitCommit();
		commit.setPull(pull != null && pull);
		commit.setBranch(branch);
		commit.setMessage(message);
		return commit;
	}
}
