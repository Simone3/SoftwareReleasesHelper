package com.utils.releaseshelper.service.jenkins;

import java.util.Map;

import com.utils.releaseshelper.connector.jenkins.JenkinsConnector;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnector.CrumbData;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnectorMock;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnectorReal;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.service.jenkins.JenkinsBuildServiceInput;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * A Service that allows to communicate with a Jenkins server
 */
public class JenkinsService implements Service {

	private final CommandLineInterface cli;
	private final JenkinsConnector connector;

	private final JenkinsConfig jenkinsConfig;
	
	// TODO move to state
	private CrumbData crumbData;

	public JenkinsService(Config config, CommandLineInterface cli) {
		
		this.cli = cli;
		this.connector = config.isTestMode() ? new JenkinsConnectorMock() : new JenkinsConnectorReal(config.getJenkins());
		this.jenkinsConfig = config.getJenkins();
	}
	
	public void startBuild(JenkinsBuildServiceInput buildInput) {
		
		String crumbUrl = jenkinsConfig.getCrumbUrl();
		String buildUrl = buildInput.getUrl();
		String username = jenkinsConfig.getUsername();
		String password = jenkinsConfig.getPassword();
		Map<String, String> parameters = buildInput.getParameters();
		
		doStartBuild(crumbUrl, buildUrl, username, password, parameters);
	}

	private void doStartBuild(String crumbUrl, String buildUrl, String username, String password, Map<String, String> parameters) {
		
		if(jenkinsConfig.isUseCrumb() && crumbData == null) {
			
			cli.println("Getting Jenkins crumb...");
			crumbData = connector.getCrumb(crumbUrl, username, password);
			cli.println("Got Jenkins crumb: %s", crumbData.getCrumb());
		}
		
		cli.println("Starting build...");
		connector.startBuild(buildUrl, username, password, crumbData, parameters);
		cli.println("Build started successfully!");
	}
}
