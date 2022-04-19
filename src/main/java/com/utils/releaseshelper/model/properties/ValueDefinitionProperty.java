package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for a value definition
 */
@Data
public class ValueDefinitionProperty {

	private String value;
	private Boolean askMe;
	private Boolean removeWhitespace;
}
