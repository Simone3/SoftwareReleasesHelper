package com.utils.releaseshelper.model.error;

import com.utils.releaseshelper.model.logic.ActionState;

import lombok.Getter;

/**
 * An Exception that triggers an action suspension
 */
@Getter
public class ActionSuspensionException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final String historyMessage;
	private final ActionState currentState;
	
	public ActionSuspensionException(String historyMessage, ActionState currentState) {
		super();
		this.historyMessage = historyMessage;
		this.currentState = currentState;
	}
}