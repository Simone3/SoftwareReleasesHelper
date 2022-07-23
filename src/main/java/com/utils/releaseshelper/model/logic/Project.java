package com.utils.releaseshelper.model.logic;

import com.utils.releaseshelper.model.view.SelectOption;

import lombok.Data;

/**
 * A project
 */
@Data
public class Project implements SelectOption {
	
	private String name;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
