package com.utils.releaseshelper.mapping.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.model.properties.JenkinsParameterProperty;
import com.utils.releaseshelper.model.properties.JenkinsProperties;
import com.utils.releaseshelper.utils.UrlUtils;
import com.utils.releaseshelper.utils.ValidationUtils;

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
		jenkinsConfig.setBaseUrl(baseUrl);
		jenkinsConfig.setCrumbUrl(fullCrumbUrl);
		jenkinsConfig.setUsername(username);
		jenkinsConfig.setPassword(password);
		jenkinsConfig.setUseCrumb(useCrumb != null && useCrumb);
		jenkinsConfig.setInsecureHttps(insecureHttps != null && insecureHttps);
		jenkinsConfig.setTimeoutMilliseconds(timeoutMilliseconds);
		return jenkinsConfig;
	}

	public static List<KeyValuePair> mapAndValidateJenkinsParameters(List<JenkinsParameterProperty> parameterProperties) {
		
		List<KeyValuePair> parameters = new ArrayList<>();
		
		if(CollectionUtils.isEmpty(parameterProperties)) {
			
			return parameters;
		}
	
		Set<String> parameterKeys = new HashSet<>();
		
		int i = 0;
		for(JenkinsParameterProperty parameterProperty: parameterProperties) {
			
			try {
				
				KeyValuePair parameter = mapAndValidateJenkinsParameter(parameterProperty);

				String parameterKeyProperty = parameterProperty.getKey();
				if(parameterKeys.contains(parameterKeyProperty)) {
					
					throw new ValidationException("Another parameter has the \"" + parameterKeyProperty + "\" key");
				}
				parameterKeys.add(parameterKeyProperty);
				
				parameters.add(parameter);
				
				i++;
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid Jenkins parameter definition at index " + i + " -> " + e.getMessage(), e);
			}
		}
		
		return parameters;
	}

	private static KeyValuePair mapAndValidateJenkinsParameter(JenkinsParameterProperty parameterProperty) {
		
		String key = ValidationUtils.notBlank(parameterProperty.getKey(), "Parameter has empty key");
		String value = ValidationUtils.notBlank(parameterProperty.getValue(), "Parameter has empty value");
		
		return new KeyValuePair(key, value);
	}
}
