package com.utils.releaseshelper.logic.action;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.action.WaitAction;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic to execute the action to wait for a specified amount of time and/or user input
 */
@Slf4j
public class WaitActionLogic extends ActionLogic<WaitAction> {

	protected WaitActionLogic(WaitAction action, Map<String, String> variables, CommandLineInterface cli) {
		
		super(action, variables, cli);
	}

	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void printDefaultActionDescription() {
		
		// Do nothing here for now
	}

	@Override
	protected void doRunAction() {
		
		sleep();
		prompt();
		
		cli.println("Wait completed");
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}

	@Override
	protected boolean confirmRunAction() {
		
		// Always runs
		return true;
	}

	private void sleep() {
		
		Integer waitMs = action.getWaitTimeMilliseconds();
		if(waitMs != null) {
			
			LocalDateTime endDate = LocalDateTime.now().plus(waitMs, ChronoUnit.MILLIS);
			cli.println("Waiting for %s ms, i.e. till %s", waitMs, endDate);
			
			try {
				
				Thread.sleep(waitMs);
			}
			catch(InterruptedException e) {
				
				log.error("Sleep interruption", e);
				cli.printError("Wait was interrupted");
				Thread.currentThread().interrupt();
			}
		}
	}

	private void prompt() {
		
		String prompt = action.getManualWaitPrompt();
		if(!StringUtils.isBlank(prompt)) {
			
			boolean confirmed = false;
			while(!confirmed) {
				
				confirmed = cli.askUserConfirmation(prompt);
			}
		}
	}
}
