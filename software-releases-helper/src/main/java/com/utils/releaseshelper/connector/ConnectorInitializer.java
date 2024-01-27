package com.utils.releaseshelper.connector;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitConnectorMock;
import com.utils.releaseshelper.connector.git.GitConnectorReal;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnector;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnectorMock;
import com.utils.releaseshelper.connector.jenkins.JenkinsConnectorReal;
import com.utils.releaseshelper.connector.process.OperatingSystemConnector;
import com.utils.releaseshelper.connector.process.OperatingSystemConnectorMock;
import com.utils.releaseshelper.connector.process.OperatingSystemConnectorReal;
import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.Config;

import lombok.RequiredArgsConstructor;

/**
 * A config class that initializes either real or mocked connectors
 */
@Configuration
@RequiredArgsConstructor
public class ConnectorInitializer {
	
	private final GlobalContext globalContext;

	@Bean
	protected JenkinsConnector jenkinsConnector() {
		
		Config config = globalContext.getDomainModel().getConfig();
		return config.isTestMode() || config.getJenkins() == null ? new JenkinsConnectorMock() : new JenkinsConnectorReal(config.getJenkins());
	}

	@Bean
	protected GitConnector gitConnector() {
		
		Config config = globalContext.getDomainModel().getConfig();
		return config.isTestMode() || config.getGit() == null ? new GitConnectorMock() : new GitConnectorReal(config.getGit());
	}

	@Bean
	protected OperatingSystemConnector operatingSystemConnector() {
		
		Config config = globalContext.getDomainModel().getConfig();
		return config.isTestMode() ? new OperatingSystemConnectorMock() : new OperatingSystemConnectorReal();
	}
}
