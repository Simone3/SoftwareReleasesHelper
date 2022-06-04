package com.utils.releaseshelper.model.logic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A value definition
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ValueDefinition {

	private String value;
	private boolean askMe;
	private boolean removeWhitespace;
}
