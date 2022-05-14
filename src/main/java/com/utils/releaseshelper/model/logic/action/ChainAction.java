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
	public boolean isGitAction() {
		
		for(Action action: actions) {
			
			if(action.isGitAction()) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isJenkinsAction() {
		
		for(Action action: actions) {
			
			if(action.isJenkinsAction()) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isMavenAction() {
		
		for(Action action: actions) {
			
			if(action.isMavenAction()) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isOperatingSystemAction() {
		
		for(Action action: actions) {
			
			if(action.isOperatingSystemAction()) {
				
				return true;
			}
		}
		
		return false;
	}
}
