package com.utils.releaseshelper.mapping;

import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.config.MavenConfig;
import com.utils.releaseshelper.model.logic.ActionFlags;
import com.utils.releaseshelper.model.logic.Category;
import com.utils.releaseshelper.model.logic.MainLogicData;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.properties.ActionProperty;
import com.utils.releaseshelper.model.properties.CategoryProperty;
import com.utils.releaseshelper.model.properties.GitProperties;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.model.properties.MavenProperties;
import com.utils.releaseshelper.model.properties.Properties;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates the main properties
 */
@UtilityClass
public class PropertiesMapperValidator {
	
	public static MainLogicData mapAndValidateProperties(Properties properties) {
		
		ValidationUtils.notNull(properties, "No properties are defined");

		Boolean testMode = properties.getTestMode();
		Boolean printPasswords = properties.getPrintPasswords();
		List<ActionProperty> actionDefinitionsProperties = properties.getActionDefinitions();
		List<CategoryProperty> categoriesProperties = properties.getCategories();
		GitProperties gitProperties = properties.getGit();
		JenkinsProperties jenkinsProperties = properties.getJenkins();
		MavenProperties mavenProperties = properties.getMaven();
		String optionalPreSelectedCategoryIndex = properties.getOptionalPreSelectedCategoryIndex();
		String optionalPreSelectedProjectIndices = properties.getOptionalPreSelectedProjectIndices();
		
		Map<String, Action> actionDefinitions = ActionMapperValidator.mapAndValidateActions(actionDefinitionsProperties, gitProperties, jenkinsProperties, mavenProperties);

		List<Category> categories = CategoryMapperValidator.mapAndValidateCategories(categoriesProperties, actionDefinitions);
		
		boolean hasGitActions = false;
		boolean hasJenkinsActions = false;
		boolean hasMavenActions = false;
		boolean hasOperatingSystemActions = false;
		for(Action actionDefinition: actionDefinitions.values()) {
			
			hasGitActions = hasGitActions || actionDefinition.isGitAction();
			hasJenkinsActions = hasJenkinsActions || actionDefinition.isJenkinsAction();
			hasMavenActions = hasMavenActions || actionDefinition.isMavenAction();
			hasOperatingSystemActions = hasOperatingSystemActions || actionDefinition.isOperatingSystemAction();
		}
		
		GitConfig gitConfig = null;
		if(hasGitActions) {
			
			gitConfig = GitMapperValidator.mapAndValidateGitConfig(gitProperties);
		}
		
		JenkinsConfig jenkinsConfig = null;
		if(hasJenkinsActions) {
			
			jenkinsConfig = JenkinsMapperValidator.mapAndValidateJenkinsConfig(jenkinsProperties);
		}
		
		MavenConfig mavenConfig = null;
		if(hasMavenActions) {
			
			mavenConfig = MavenMapperValidator.mapAndValidateMavenConfig(mavenProperties);
		}
		
		Config config = new Config();
		config.setGit(gitConfig);
		config.setJenkins(jenkinsConfig);
		config.setMaven(mavenConfig);
		config.setTestMode(testMode != null && testMode);
		config.setPrintPasswords(printPasswords != null && printPasswords);
		
		ActionFlags actionFlags = new ActionFlags();
		actionFlags.setGitActions(hasGitActions);
		actionFlags.setJenkinsActions(hasJenkinsActions);
		actionFlags.setMavenActions(hasMavenActions);
		actionFlags.setOperatingSystemActions(hasOperatingSystemActions);
		
		MainLogicData mainData = new MainLogicData();
		mainData.setCategories(categories);
		mainData.setConfig(config);
		mainData.setActionFlags(actionFlags);
		mainData.setOptionalPreSelectedCategoryIndex(optionalPreSelectedCategoryIndex);
		mainData.setOptionalPreSelectedProjectIndices(optionalPreSelectedProjectIndices);
		return mainData;
	}
}
