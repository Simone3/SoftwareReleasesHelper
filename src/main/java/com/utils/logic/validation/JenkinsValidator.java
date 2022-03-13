package com.utils.logic.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.jenkins.JenkinsData;
import com.utils.model.jenkins.JenkinsParameter;
import com.utils.model.main.Action;

class JenkinsValidator extends ValidationErrorGenerator {

	public void validateJenkinsData(JenkinsData jenkinsData) {
		
		if(jenkinsData == null) {
			
			throw jenkinsDataError("is empty");
		}
		
		if(StringUtils.isBlank(jenkinsData.getBaseUrl())) {
			
			throw jenkinsDataError("does not have a base URL");
		}
		
		if(StringUtils.isBlank(jenkinsData.getCrumbUrl())) {
			
			throw jenkinsDataError("does not have a crumb URL");
		}
		
		if(StringUtils.isBlank(jenkinsData.getUsername())) {
			
			throw jenkinsDataError("does not have a username");
		}
		
		if(StringUtils.isBlank(jenkinsData.getPassword())) {
			
			throw jenkinsDataError("does not have a password");
		}
	}

	public void validateJenkinsAction(Action action, String categoryName, String projectName, int actionIndex) {
		
		var buildUrl = action.getBuildUrl();
		var parameters = action.getBuildParameters();
		
		if(StringUtils.isBlank(buildUrl)) {
			
			throw actionError(categoryName, projectName, actionIndex, "does not have a URL");
		}
		
		validateParameters(parameters, categoryName, projectName, actionIndex);
	}
	
	private void validateParameters(List<JenkinsParameter> parameters, String categoryName, String projectName, int actionIndex) {
		
		if(parameters == null || parameters.isEmpty()) {
			
			throw actionError(categoryName, projectName, actionIndex, "does not have any parameter");
		}
		
		Map<String, Void> keys = new HashMap<>();
		
		for(var i = 0; i < parameters.size(); i++) {
			
			var parameter = parameters.get(i);
			validateParameter(parameter, categoryName, projectName, actionIndex, i);
			
			String key = parameter.getKey();
			if(keys.containsKey(key)) {
				
				throw parameterError(categoryName, projectName, actionIndex, i, "has the same key of a previous parameter");
			}
			keys.put(key, null);
		}
	}
	
	private void validateParameter(JenkinsParameter parameter, String categoryName, String projectName, int actionIndex, int parameterIndex) {
		
		if(parameter == null) {
			
			throw parameterError(categoryName, projectName, actionIndex, parameterIndex, "is empty");
		}
		
		String key = parameter.getKey();
		boolean hasFixedValue = !StringUtils.isBlank(parameter.getValue());
		boolean hasDynamicValue = parameter.isAskMe();
		
		if(StringUtils.isBlank(key)) {

			throw parameterError(categoryName, projectName, actionIndex, parameterIndex, "does not have a key");
		}
		
		if((hasFixedValue && hasDynamicValue) || (!hasFixedValue && !hasDynamicValue)) {

			throw parameterError(categoryName, projectName, actionIndex, parameterIndex, "must EITHER have a fixed value or the run-time prompt");
		}
	}
}
