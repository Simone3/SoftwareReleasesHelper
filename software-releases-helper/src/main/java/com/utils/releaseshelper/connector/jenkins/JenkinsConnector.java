package com.utils.releaseshelper.connector.jenkins;

import java.util.List;

import com.utils.releaseshelper.connector.Connector;
import com.utils.releaseshelper.model.logic.JenkinsCrumbData;
import com.utils.releaseshelper.model.misc.KeyValuePair;

/**
 * The connector to interact with a Jenkins server
 */
public interface JenkinsConnector extends Connector {

	JenkinsCrumbData getCrumb(String crumbUrl, String username, String password);
	
	void startBuild(String buildUrl, String username, String password, JenkinsCrumbData crumbData, List<KeyValuePair> parameters);
}
