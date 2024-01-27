package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for a variable definition
 */
@Data
public class VariableDefinitionProperty {

	private String key;
	private VariableDefinitionTypeProperty type;
	private Boolean removeWhitespace;
	private String value;
	private List<String> options;
}
