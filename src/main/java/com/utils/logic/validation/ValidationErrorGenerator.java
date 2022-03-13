package com.utils.logic.validation;

class ValidationErrorGenerator {
	
	private static final String CATEGORY_INDEX_ERROR = "Category at index %s %s";
	private static final String CATEGORY_NAME_ERROR  = "Category \"%s\" %s";
	private static final String PROJECT_INDEX_ERROR  = "Project at index %s of category \"%s\" %s";
	private static final String PROJECT_NAME_ERROR   = "Project \"%s\" of category \"%s\" %s";
	private static final String ACTION_ERROR         = "Action at index %s of project \"%s\" of category \"%s\" %s";
	private static final String OPERATION_ERROR      = "Operation at index %s of action at index %s of project \"%s\" of category \"%s\" %s";
	private static final String PARAMETER_ERROR      = "Parameter at index %s of action at index %s of project \"%s\" of category \"%s\" %s";
	private static final String GIT_DATA_ERROR       = "Git configuration %s";
	private static final String JENKINS_DATA_ERROR   = "Jenkins configuration %s";
	
	protected RuntimeException error(String message) {
		
		return new IllegalArgumentException(message);
	}
	
	protected RuntimeException topLevelError(String message) {
		
		return error(message);
	}
	
	protected RuntimeException categoryError(int categoryIndex, String message) {
		
		return error(String.format(CATEGORY_INDEX_ERROR, categoryIndex, message));
	}
	
	protected RuntimeException categoryError(String categoryName, String message) {
		
		return error(String.format(CATEGORY_NAME_ERROR, categoryName, message));
	}
	
	protected RuntimeException projectError(String categoryName, int projectIndex, String message) {
		
		return error(String.format(PROJECT_INDEX_ERROR, projectIndex, categoryName, message));
	}
	
	protected RuntimeException projectError(String categoryName, String projectName, String message) {
		
		return error(String.format(PROJECT_NAME_ERROR, projectName, categoryName, message));
	}
	
	protected RuntimeException actionError(String categoryName, String projectName, int actionIndex, String message) {
		
		return error(String.format(ACTION_ERROR, actionIndex, projectName, categoryName, message));
	}
	
	protected RuntimeException operationError(String categoryName, String projectName, int actionIndex, int operationIndex, String message) {
		
		return error(String.format(OPERATION_ERROR, operationIndex, actionIndex, projectName, categoryName, message));
	}
	
	protected RuntimeException parameterError(String categoryName, String projectName, int actionIndex, int parameterIndex, String message) {
		
		return error(String.format(PARAMETER_ERROR, parameterIndex, actionIndex, projectName, categoryName, message));
	}
	
	protected RuntimeException gitDataError(String message) {
		
		return error(String.format(GIT_DATA_ERROR, message));
	}
	
	protected RuntimeException jenkinsDataError(String message) {
		
		return error(String.format(JENKINS_DATA_ERROR, message));
	}
}
