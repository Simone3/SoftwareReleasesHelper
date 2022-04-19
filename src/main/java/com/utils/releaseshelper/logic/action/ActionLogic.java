package com.utils.releaseshelper.logic.action;

import java.util.Map;

import org.springframework.util.Assert;

import com.utils.releaseshelper.logic.Logic;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Generic logic implementation for an action
 * Its purpose is to run the business logic of a specific type of action
 * It is a stateful class (per action)
 * @param <A> the action type
 */
public abstract class ActionLogic<A extends Action> implements Logic {
	
	private static final String DEFAULT_CONFIRM_PROMPT = "Start action";
	
	protected final A action;
	protected final Map<String, String> variables;
	protected final CommandLineInterface cli;
	
	protected ActionLogic(A action, Map<String, String> variables, CommandLineInterface cli) {
		
		Assert.notNull(action, "Action cannot be null");
		Assert.notNull(variables, "Variables cannot be null");
		
		this.action = action;
		this.variables = variables;
		this.cli = cli;
	}
	
	public void run() {
		
		beforeAction();
		
		printActionDescription();
		
		if(confirmRunAction()) {
			
			doRunAction();
		}
		else {
			
			cli.println("Action skipped");
		}
		
		afterAction();
	}
	
	protected abstract void beforeAction();
	
	protected abstract void printActionDescription();
	
	protected boolean confirmRunAction() {
		
		if(action.isSkipConfirmation()) {
			
			return true;
		}
		
		return cli.askUserConfirmation(getConfirmationPrompt());
	}

	protected String getConfirmationPrompt() {
		
		return DEFAULT_CONFIRM_PROMPT;
	}
	
	protected abstract void doRunAction();
	
	protected abstract void afterAction();
}
