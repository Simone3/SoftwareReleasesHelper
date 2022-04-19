package com.utils.releaseshelper.model.logic.action;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action that contains other sub-actions
 * This is useful to have a single confirmation message for several actions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChainAction extends Action {

	private List<Action> actions;

	@Override
	public String getTypeDescription() {
		
		return "Chain";
	}

	@Override
	public boolean requiresGitConfig() {
		
		for(Action action: actions) {
			
			if(action.requiresGitConfig()) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean requiresJenkinsConfig() {
		
		for(Action action: actions) {
			
			if(action.requiresJenkinsConfig()) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean requiresMavenConfig() {
		
		for(Action action: actions) {
			
			if(action.requiresMavenConfig()) {
				
				return true;
			}
		}
		
		return false;
	}
}
