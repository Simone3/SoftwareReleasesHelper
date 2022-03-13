package com.utils.model.main;

import java.util.List;

import com.utils.model.view.SelectOption;

import lombok.Data;

@Data
public class Category implements SelectOption {

	private String name;
	private List<Project> projects;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
