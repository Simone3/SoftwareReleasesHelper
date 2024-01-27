package com.utils.releaseshelper.context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.mapping.domain.PropertiesMapperValidator;
import com.utils.releaseshelper.model.domain.Config;
import com.utils.releaseshelper.model.domain.DomainModel;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.GlobalState;
import com.utils.releaseshelper.model.properties.Properties;
import com.utils.releaseshelper.view.userinterface.CommandLineInterface;
import com.utils.releaseshelper.view.userinterface.UserInterface;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * The main Spring component that initializes and holds the application domain and global state models
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalContext {

	private final CommandLineInterface cli;
	private final Properties properties;
	
	private DomainModel domainModel;
	private GlobalState globalState;
	
	public DomainModel getDomainModel() {
		
		return domainModel;
	}

	public GlobalState getGlobalState() {
		
		return globalState;
	}
	
	@PostConstruct
	private void init() {
		
		// Parse and validate properties
		try {
			
			domainModel = PropertiesMapperValidator.mapAndValidateProperties(properties);
		}
		catch(Exception e) {

			log.error("Validation error", e);
			cli.printError("Invalid configuration: %s", e.getMessage());
			printSampleConfiguration(cli);
			throw new BusinessException("Cannot init global context", e);
		}
		
		// Print the current settings
		printConfig();
		
		// Initialize the global state
		globalState = new GlobalState();
	}

	private void printConfig() {
		
		Config config = domainModel.getConfig();
		boolean testMode = config.isTestMode();
		boolean printPasswords = config.isPrintPasswords();
		JenkinsConfig jenkins = config.getJenkins();
		GitConfig git = config.getGit();
		
		if(jenkins != null) {

			String password = StringUtils.defaultIfBlank(jenkins.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.printLine("Loaded Jenkins config:");
			cli.printLine("  - Crumb URL: %s", StringUtils.defaultIfBlank(jenkins.getCrumbUrl(), ""));
			cli.printLine("  - Username: %s", StringUtils.defaultIfBlank(jenkins.getUsername(), ""));
			cli.printLine("  - Password: %s", password);
			cli.printLine("  - Use crumb: %s", jenkins.isUseCrumb());
			cli.printLine("  - Insecure HTTPS: %s", jenkins.isInsecureHttps());
			cli.printLine("  - Timeout (ms): %s", jenkins.getTimeoutMilliseconds());
			cli.printLine();
		}
		
		if(git != null) {

			String password = StringUtils.defaultIfBlank(git.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.printLine("Loaded Git config:");
			cli.printLine("  - Username: %s", StringUtils.defaultIfBlank(git.getUsername(), ""));
			cli.printLine("  - Password: %s", password);
			cli.printLine("  - Merge message: %s", StringUtils.defaultIfBlank(git.getMergeMessage(), ""));
			cli.printLine("  - Timeout (ms): %s", git.getTimeoutMilliseconds());
			cli.printLine();
		}
		
		if(testMode) {
			
			cli.printLine("*****************************************************************");
			cli.printLine("*** WARNING: test mode is active, all actions will do nothing ***");
			cli.printLine("*****************************************************************");
			cli.printLine();
		}
	}
	
	@SneakyThrows
	private void printSampleConfiguration(UserInterface cli) {
		
		cli.printLine();
		cli.printLine("Define an application.yml in the JAR folder similar to this one:");
		
		cli.printSeparator();

		ClassPathResource sampleConfiguration = new ClassPathResource("application-sample.yml", this.getClass().getClassLoader());
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(sampleConfiguration.getInputStream()))) {
			
			String line = reader.readLine();
			while(line != null) {
					
				cli.printLine(line);
				line = reader.readLine();
			}
		}
		
		cli.printSeparator();
	}
}
