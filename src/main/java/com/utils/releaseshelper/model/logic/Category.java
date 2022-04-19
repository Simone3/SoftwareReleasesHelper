package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.view.SelectOption;

import lombok.Data;

/**
 * A category: top-level user choice that contains one or more projects
 */
@Data
public class Category implements SelectOption {

	private String name;
	private List<Project> projects;
	
	@Override
	public String getOptionName() {

		return name;
	}
}
