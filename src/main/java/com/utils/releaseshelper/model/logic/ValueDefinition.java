package com.utils.releaseshelper.model.logic;

import lombok.Data;

/**
 * A value definition
 */
@Data
public class ValueDefinition {

	private String value;
	private boolean askMe;
	private boolean removeWhitespace;
}
