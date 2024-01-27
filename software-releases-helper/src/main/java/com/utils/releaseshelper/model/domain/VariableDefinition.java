package com.utils.releaseshelper.model.domain;

import java.util.List;

import lombok.Data;

/**
 * A variable definition
 */
@Data
public class VariableDefinition {

	private String key;
	private VariableDefinitionType type;
	private boolean removeWhitespace;
	private String value;
	private List<String> options;
}
