package com.utils.releaseshelper.model.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A property for a variable definition
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VariableDefinitionProperty extends ValueDefinitionProperty {

	private String key;
}
