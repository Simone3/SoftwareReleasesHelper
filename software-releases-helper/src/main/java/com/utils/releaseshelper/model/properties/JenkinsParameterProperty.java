package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for a Jenkins parameter
 */
@Data
public class JenkinsParameterProperty {

	private String key;
	private String value;
}
