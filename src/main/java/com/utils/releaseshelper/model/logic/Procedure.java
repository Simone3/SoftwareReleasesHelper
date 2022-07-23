package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.logic.step.Step;
import com.utils.releaseshelper.model.view.SelectOption;

import lombok.Data;

/**
 * A procedure
 */
@Data
public class Procedure implements SelectOption {
	
	private String name;
	private List<Step> steps;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
