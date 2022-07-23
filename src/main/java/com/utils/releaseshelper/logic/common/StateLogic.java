package com.utils.releaseshelper.logic.common;

import com.utils.releaseshelper.logic.Logic;

/**
 * Common implementation of a state-logic
 * It offers helper methods to run a multi-state process
 * @param <S> the state enum
 */
public abstract class StateLogic<S extends Enum<S>> implements Logic {
	
	private static final int SAME_STEP_LIMIT = 1000;

	private final S initialState;
	private final S exitState;
	
	private int sameStateCounter = 0;
	private S currentState;
	
	protected StateLogic(S initialState, S exitState) {
		
		if(initialState == null) {
			
			throw new IllegalStateException("Initial state cannot be null");
		}
		
		if(exitState == null) {
			
			throw new IllegalStateException("Exit state cannot be null");
		}
		
		this.initialState = initialState;
		this.currentState = initialState;
		this.exitState = exitState;
	}
	
	protected abstract S processCurrentState(S currentState);
	
	protected void loopStates() {
		
		while(currentState != exitState) {
			
			S newState = processCurrentState(currentState);
			
			if(newState == null) {
				
				throw new IllegalStateException("New state cannot be null");
			}
			
			if(currentState == newState) {
				
				if(sameStateCounter > SAME_STEP_LIMIT) {
					
					throw new IllegalStateException("Looped on " + currentState + " " + SAME_STEP_LIMIT + " times, potential infinite loop?");
				}
				
				sameStateCounter++;
			}
			else {
			
				sameStateCounter = 0;
				currentState = newState;
			}
		}
		
		resetState();
	}
	
	protected void resetState() {

		sameStateCounter = 0;
		currentState = initialState;
	}
}
