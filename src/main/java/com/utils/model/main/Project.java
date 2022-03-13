package com.utils.model.main;

import java.util.List;

import com.utils.model.view.SelectOption;

import lombok.Data;

@Data
public class Project implements SelectOption {

	private String name;
	private List<Action> actions;
	
	@Override
	public String getOptionName() {
		
		return name;
	}
}
