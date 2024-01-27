package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for Jenkins Build Action data
 */
@Data
public class JenkinsBuildDefinitionProperty {

	private String url;
	private List<JenkinsParameterProperty> parameters;
}
