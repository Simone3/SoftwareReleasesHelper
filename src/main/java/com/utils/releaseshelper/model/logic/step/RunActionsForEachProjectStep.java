package com.utils.releaseshelper.model.logic.step;

import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.Action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Step to run one or more actions for one or more projects
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RunActionsForEachProjectStep extends Step {
	
	private Map<String, List<Action>> projectActions;
	
	@Override
	public String getTypeDescription() {
		
		return "Run Actions For Projects";
	}
}
