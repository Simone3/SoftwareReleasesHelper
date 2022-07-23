package com.utils.releaseshelper.model.logic.step;

import java.util.List;

import com.utils.releaseshelper.model.logic.action.Action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Step to run one or more actions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RunActionsStep extends Step {
	
	private List<Action> actions;
	
	@Override
	public String getTypeDescription() {
		
		return "Run Actions";
	}
}
