package com.utils.releaseshelper.connector.jenkins;

import java.util.Map;

import com.utils.releaseshelper.connector.Connector;

/**
 * The connector to interact with a Jenkins server
 */
public interface JenkinsConnector extends Connector {

	String getCrumb(String crumbUrl, String username, String password);
	
	void startBuild(String buildUrl, String username, String password, String crumb, Map<String, String> parameters);
}
