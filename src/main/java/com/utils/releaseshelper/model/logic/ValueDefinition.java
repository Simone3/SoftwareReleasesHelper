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

	private int id; // this is needed for putting ValueDefinitions as keys of Maps!
	private String staticContent;
	private boolean askMe;
	private boolean removeWhitespace;
}
