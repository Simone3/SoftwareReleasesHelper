package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.view.SelectOption;

import lombok.Data;

/**
 * A project: it contains a list of one or more actions to run
 */
@Data
public class Project implements SelectOption {

	private String name;
	private List<Action> actions;
	
	@Override
	public String getOptionName() {

		return name;
	}
}
