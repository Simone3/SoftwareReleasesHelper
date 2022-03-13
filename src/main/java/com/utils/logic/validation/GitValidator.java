package com.utils.logic.validation;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.git.GitData;
import com.utils.model.git.GitOperation;
import com.utils.model.main.Action;

class GitValidator extends ValidationErrorGenerator {

	public void validateGitData(GitData gitData) {
		
		if(gitData == null) {
			
			throw gitDataError("is empty");
		}
		
		if(StringUtils.isBlank(gitData.getMergeMessage())) {
			
			throw gitDataError("does not have a merge message");
		}
		
		if(StringUtils.isBlank(gitData.getUsername())) {
			
			throw gitDataError("does not have a username");
		}
		
		if(StringUtils.isBlank(gitData.getPassword())) {
			
			throw gitDataError("does not have a password");
		}
	}

	public void validateGitAction(Action action, String categoryName, String projectName, int actionIndex) {

		var repositoryFolder = action.getGitRepositoryFolder();
		var operations = action.getGitOperations();
		
		if(StringUtils.isBlank(repositoryFolder)) {
			
			throw actionError(categoryName, projectName, actionIndex, "does not have a folder");
		}
		
		validateOperations(operations, categoryName, projectName, actionIndex);
	}

	private void validateOperations(List<GitOperation> operations, String categoryName, String projectName, int actionIndex) {
		
		if(operations == null || operations.isEmpty()) {
			
			throw actionError(categoryName, projectName, actionIndex, "does not have any operation");
		}
		
		for(var i = 0; i < operations.size(); i++) {
			
			var operation = operations.get(i);
			validateOperation(operation, categoryName, projectName, actionIndex, i);
		}
	}

	private void validateOperation(GitOperation operation, String categoryName, String projectName, int actionIndex, int operationIndex) {
		
		var type = operation.getType();
		
		if(type == null) {
			
			throw operationError(categoryName, projectName, actionIndex, operationIndex, "does not have a type");
		}
		
		switch(type) {
		
			case MERGE:
				validateMergeOperation(operation, categoryName, projectName, actionIndex, operationIndex);
				break;
			
			default:
				throw new IllegalStateException("Unrecognized operation type " + type);
		}
	}

	private void validateMergeOperation(GitOperation operation, String categoryName, String projectName, int actionIndex, int operationIndex) {
		
		var sourceBranch = operation.getSourceBranch();
		var targetBranch = operation.getTargetBranch();
		
		if(StringUtils.isBlank(sourceBranch)) {
			
			throw operationError(categoryName, projectName, actionIndex, operationIndex, "does not have a source branch");
		}
		
		if(StringUtils.isBlank(targetBranch)) {
			
			throw operationError(categoryName, projectName, actionIndex, operationIndex, "does not have a target branch");
		}
		
		if(sourceBranch.trim().equals(targetBranch.trim())) {
			
			throw operationError(categoryName, projectName, actionIndex, operationIndex, "has the same source and target branches");
		}
	}
}
