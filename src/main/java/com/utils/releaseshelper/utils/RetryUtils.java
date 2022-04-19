package com.utils.releaseshelper.utils;

import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper util to define retry logic
 */
@Slf4j
@UtilityClass
public class RetryUtils {

	public static boolean retry(CommandLineInterface cli, String retryMessage, String errorMessage, Runnable retryableAction) {
		
		boolean first = true;
		
		while(first || cli.askUserConfirmation(retryMessage)) {
			
			if(first) {
				
				first = false;
			}
			
			try {
				
				retryableAction.run();
				return true;
			}
			catch(Exception e) {
				
				cli.printError(errorMessage + ": %s", e.getMessage());
				log.error(errorMessage, e);
			}
		}
		
		return false;
	}
}
