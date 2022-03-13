package com.utils.logic.jenkins;

import java.util.Map;

interface JenkinsService {

	String getCrumb(String crumbUrl, String username, String password);
	
	void startBuild(String buildUrl, String username, String password, String crumb, Map<String, String> parameters);
}
