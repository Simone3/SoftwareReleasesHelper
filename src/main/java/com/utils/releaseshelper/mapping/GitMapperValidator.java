package com.utils.releaseshelper.mapping;

import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.git.GitCommit;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
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
	
	public static GitCommit mapAndValidateGitCommit(GitCommitProperty commitProperty) {

		String branchProperty = commitProperty.getBranch();
		String messageProperty = commitProperty.getCommitMessage();
		Boolean pull = commitProperty.getPull();
		
		ValueDefinition branch;
		try {
			
			branch = VariablesMapperValidator.mapAndValidateValueDefinition(branchProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid commit branch -> " + e.getMessage(), e);
		}
		
		ValueDefinition message;
		try {
			
			message = VariablesMapperValidator.mapAndValidateValueDefinition(messageProperty);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid commit message -> " + e.getMessage(), e);
		}
		
		GitCommit commit = new GitCommit();
		commit.setPull(pull != null && pull);
		commit.setBranch(branch);
		commit.setMessage(message);
		return commit;
	}
}
