package com.utils.releaseshelper.logic.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.action.ChainAction;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute a chain of actions, with a single confirmation request
 * All sub-actions are executed in "sync" (first all beforeAction, then all doRunAction, etc.)
 */
public class ChainActionLogic extends ActionLogic<ChainAction> {
	
	private final List<ActionLogic<?>> subActionsLogic;

	protected ChainActionLogic(ChainAction action, Map<String, String> variables, CommandLineInterface cli, List<ActionLogic<?>> subActionsLogic) {
		
		super(action, variables, cli);
		this.subActionsLogic = Collections.unmodifiableList(subActionsLogic);
	}

	@Override
	protected void beforeAction() {
		
		for(ActionLogic<?> subActionLogic: subActionsLogic) {
			
			subActionLogic.beforeAction();
		}
	}

	@Override
	protected void printActionDescription() {
		
		for(ActionLogic<?> subActionLogic: subActionsLogic) {
			
			subActionLogic.printActionDescription();
		}
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Start actions chain";
	}

	@Override
	protected void doRunAction() {
		
		for(int i = 0; i < subActionsLogic.size(); i++) {
			
			Action subAction = action.getActions().get(i);
			ActionLogic<?> subActionLogic = subActionsLogic.get(i);
			
			String actionDescription = "sub-action \"" + subAction.getName() + "\" (" + subAction.getTypeDescription() + ")";

			cli.startIdentationGroup("Start %s", actionDescription);
			
			subActionLogic.doRunAction();
			
			cli.endIdentationGroup("End %s", actionDescription);
		}
	}

	@Override
	protected void afterAction() {
		
		for(ActionLogic<?> subActionLogic: subActionsLogic) {
			
			subActionLogic.afterAction();
		}
	}
}
