package com.utils.releaseshelper.mapping.domain;

import java.util.List;

import com.utils.releaseshelper.model.domain.Action;
import com.utils.releaseshelper.model.domain.Config;
import com.utils.releaseshelper.model.domain.DomainModel;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.domain.LogicData;
import com.utils.releaseshelper.model.properties.ActionProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.model.properties.Properties;
import com.utils.releaseshelper.utils.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates the main properties
 */
@UtilityClass
public class PropertiesMapperValidator {
	
	public static DomainModel mapAndValidateProperties(Properties properties) {
		
		ValidationUtils.notNull(properties, "No properties are defined");

		Boolean webGui = properties.getWebGui();
		Boolean testMode = properties.getTestMode();
		Boolean printPasswords = properties.getPrintPasswords();
		List<ActionProperty> actionDefinitionsProperties = properties.getActionDefinitions();
		JenkinsProperties jenkinsProperties = properties.getJenkins();
		GitProperties gitProperties = properties.getGit();
		
		JenkinsConfig jenkinsConfig = null;
		if(jenkinsProperties != null) {
			
			jenkinsConfig = JenkinsMapperValidator.mapAndValidateJenkinsConfig(jenkinsProperties);
		}	
		
		GitConfig gitConfig = null;
		if(gitProperties != null) {
			
			gitConfig = GitMapperValidator.mapAndValidateGitConfig(gitProperties);
		}
		
		Config config = new Config();
		config.setWebGui(webGui == null || webGui); // Defaults to true
		config.setTestMode(testMode != null && testMode); // Defaults to false
		config.setPrintPasswords(printPasswords != null && printPasswords); // Defaults to false
		config.setJenkins(jenkinsConfig);
		config.setGit(gitConfig);
		
		List<Action> actionDefinitions = ActionMapperValidator.mapAndValidateActions(actionDefinitionsProperties, config);

		LogicData logicData = new LogicData();
		logicData.setActions(actionDefinitions);
		
		DomainModel domainModel = new DomainModel();
		domainModel.setConfig(config);
		domainModel.setLogicData(logicData);

		return domainModel;
	}
}
