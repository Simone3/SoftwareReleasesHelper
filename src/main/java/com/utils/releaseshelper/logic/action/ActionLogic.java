package com.utils.releaseshelper.logic.action;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.utils.releaseshelper.logic.Logic;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.utils.ValuesDefiner;
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
	
	/**
	 * Main method to run all action logic
	 */
	public void run() {
		
		beforeAction();
		
		ValuesDefiner valuesDefiner = new ValuesDefiner(cli, variables);
		registerValueDefinitions(valuesDefiner);
		
		printDescriptionAndHandleValueDefinitions(valuesDefiner);

		if(confirmRunAction()) {
			
			doRunAction(valuesDefiner);
		}
		else {
			
			cli.println("Action skipped");
		}
		
		afterAction();
	}

	/**
	 * Runs at the beginning of the action logic
	 */
	protected abstract void beforeAction();
	
	/**
	 * Adds any dynamic value definition, to be processed by the centralized logic
	 */
	protected abstract void registerValueDefinitions(ValuesDefiner definer);

	/**
	 * Prints the action description
	 */
	protected abstract void printActionDescription(ValuesDefiner valuesDefiner);

	/**
	 * Defines the confirmation prompt
	 */
	protected String getConfirmationPrompt() {
		
		return DEFAULT_CONFIRM_PROMPT;
	}
	
	/**
	 * Runs the action-specific business logic
	 */
	protected abstract void doRunAction(ValuesDefiner valuesDefiner);
	
	/**
	 * Runs at the end of the action logic
	 */
	protected abstract void afterAction();
	
	/**
	 * Defines if the confirm prompt should be displayed
	 */
	protected boolean confirmRunAction() {
		
		if(action.isSkipConfirmation()) {
			
			return true;
		}
		
		return cli.askUserConfirmation(getConfirmationPrompt());
	}
	
	private void printDescriptionAndHandleValueDefinitions(ValuesDefiner valuesDefiner) {
		
		if(valuesDefiner.hasAskMeValues()) {
			
			printActionDescription(valuesDefiner);
			
			valuesDefiner.defineAskMeValues();
			
			cli.println("All values defined! Final action recap:");
			
			printCustomActionDescription(valuesDefiner);
		}
		else {
			
			printCustomActionDescription(valuesDefiner);
		}
	}
	
	private void printCustomActionDescription(ValuesDefiner valuesDefiner) {
		
		String customDescription = action.getCustomDescription();
		if(StringUtils.isBlank(customDescription)) {
			
			printActionDescription(valuesDefiner);
		}
		else {
			
			cli.println(customDescription);
			cli.println();
		}
	}
}
