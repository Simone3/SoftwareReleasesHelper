package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A variable definition
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VariableDefinition extends ValueDefinition {

	private String key;
}
