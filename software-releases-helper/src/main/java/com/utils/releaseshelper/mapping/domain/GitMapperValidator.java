package com.utils.releaseshelper.mapping.domain;

import com.utils.releaseshelper.model.domain.GitCommit;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.properties.GitCommitProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.utils.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates Git properties
 */
@UtilityClass
public class GitMapperValidator {

	public static GitConfig mapAndValidateGitConfig(GitProperties gitProperties) {
		
		ValidationUtils.notNull(gitProperties, "No Git properties are defined");
		
		String basePath = gitProperties.getBasePath();
		String username = ValidationUtils.notBlank(gitProperties.getUsername(), "Git username is empty");
		String password = ValidationUtils.notBlank(gitProperties.getPassword(), "Git password is empty");
		String mergeMessage = ValidationUtils.notBlank(gitProperties.getMergeMessage(), "Git merge message is empty");
		Integer timeoutMilliseconds = ValidationUtils.positive(gitProperties.getTimeoutMilliseconds(), "Git timeout is empty or invalid");
		
		GitConfig gitConfig = new GitConfig();
		gitConfig.setBasePath(basePath);
		gitConfig.setUsername(username);
		gitConfig.setPassword(password);
		gitConfig.setMergeMessage(mergeMessage);
		gitConfig.setTimeoutMilliseconds(timeoutMilliseconds);
		return gitConfig;
	}

	public static GitCommit mapAndValidateGitCommit(GitCommitProperty commitProperty) {
		
		String branchProperty = ValidationUtils.notBlank(commitProperty.getBranch(), "Git commit branch is empty");
		String messageProperty = ValidationUtils.notBlank(commitProperty.getCommitMessage(), "Git commit messsage is empty");
		Boolean pull = commitProperty.getPull();
		
		GitCommit commit = new GitCommit();
		commit.setBranch(branchProperty);
		commit.setMessage(messageProperty);
		commit.setPull(pull != null && pull);
		return commit;
	}
}
