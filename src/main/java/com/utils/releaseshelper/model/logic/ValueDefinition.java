package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.view.SelectOption;

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
	private ValueDefinitionType definitionType;
	private List<SelectOption> options;
	private boolean removeWhitespace;
	
	public boolean isAskMe() {
		
		return definitionType == ValueDefinitionType.DYNAMIC_TEXT || definitionType == ValueDefinitionType.DYNAMIC_SELECT;
	}
}
