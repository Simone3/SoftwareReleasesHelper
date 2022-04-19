package com.utils.releaseshelper.logic.common;

import com.utils.releaseshelper.logic.Logic;

/**
 * Common implementation of a step-logic
 * It offers helper methods to run a multi-step process
 * @param <S> the step enum
 */
public abstract class StepLogic<S extends Enum<S>> implements Logic {
	
	private static final int SAME_STEP_LIMIT = 1000;

	private final S initialStep;
	private final S exitStep;
	
	private int sameStepCounter = 0;
	private S currentStep;
	
	protected StepLogic(S initialStep, S exitStep) {
		
		if(initialStep == null) {
			
			throw new IllegalStateException("Initial step cannot be null");
		}
		
		if(exitStep == null) {
			
			throw new IllegalStateException("Exit step cannot be null");
		}
		
		this.initialStep = initialStep;
		this.currentStep = initialStep;
		this.exitStep = exitStep;
	}
	
	protected abstract S processCurrentStep(S currentStep);
	
	protected void loopSteps() {
		
		while(currentStep != exitStep) {
			
			S newStep = processCurrentStep(currentStep);
			
			if(newStep == null) {
				
				throw new IllegalStateException("New step cannot be null");
			}
			
			if(currentStep == newStep) {
				
				if(sameStepCounter > SAME_STEP_LIMIT) {
					
					throw new IllegalStateException("Looped on " + currentStep + " " + SAME_STEP_LIMIT + " times, potential infinite loop?");
				}
				
				sameStepCounter++;
			}
			else {
			
				sameStepCounter = 0;
				currentStep = newStep;
			}
		}
		
		resetStep();
	}
	
	protected void resetStep() {

		sameStepCounter = 0;
		currentStep = initialStep;
	}
}
