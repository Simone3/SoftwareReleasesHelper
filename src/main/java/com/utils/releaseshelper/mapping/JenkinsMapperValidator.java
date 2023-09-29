package com.utils.releaseshelper.mapping;

import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.utils.UrlUtils;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates Jenkins properties
 */
@UtilityClass
public class JenkinsMapperValidator {

	public static JenkinsConfig mapAndValidateJenkinsConfig(JenkinsProperties jenkinsProperties) {
		
		ValidationUtils.notNull(jenkinsProperties, "No Jenkins properties are defined");
		
		String baseUrl = ValidationUtils.notBlank(jenkinsProperties.getBaseUrl(), "Jenkins base URL is empty");
		String crumbUrl = ValidationUtils.notBlank(jenkinsProperties.getCrumbUrl(), "Jenkins crumb URL is empty");
		String username = ValidationUtils.notBlank(jenkinsProperties.getUsername(), "Jenkins username is empty");
		String password = ValidationUtils.notBlank(jenkinsProperties.getPassword(), "Jenkins password is empty");
		Boolean useCrumb = jenkinsProperties.getUseCrumb();
		Boolean insecureHttps = jenkinsProperties.getInsecureHttps();
		Integer timeoutMilliseconds = ValidationUtils.positive(jenkinsProperties.getTimeoutMilliseconds(), "Jenkins timeout is empty or invalid");
		
		String fullCrumbUrl = UrlUtils.getFullUrl(baseUrl, crumbUrl);
		
		JenkinsConfig jenkinsConfig = new JenkinsConfig();
		jenkinsConfig.setCrumbUrl(fullCrumbUrl);
		jenkinsConfig.setUsername(username);
		jenkinsConfig.setPassword(password);
		jenkinsConfig.setUseCrumb(useCrumb != null && useCrumb);
		jenkinsConfig.setInsecureHttps(insecureHttps != null && insecureHttps);
		jenkinsConfig.setTimeoutMilliseconds(timeoutMilliseconds);
		return jenkinsConfig;
	}
}
