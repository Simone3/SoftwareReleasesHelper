package com.utils.releaseshelper.utils;

import java.util.List;

import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper util to run logic that can be skipped, retried or stopped
 */
@Slf4j
@UtilityClass
public class ErrorRemediationUtils {

	public static boolean runWithErrorRemediation(CommandLineInterface cli, String errorMessage, ErrorRemediationOption[] options, Runnable runnable) {
		
		return runWithErrorRemediation(cli, errorMessage, List.of(options), runnable);
	}

	public static boolean runWithErrorRemediation(CommandLineInterface cli, String errorMessage, List<ErrorRemediationOption> options, Runnable runnable) {
		
		boolean run = true;
		
		while(run) {
			
			try {
				
				runnable.run();
				return true;
			}
			catch(Exception e) {
				
				run = handleError(e, cli, errorMessage, options);
			}
		}
		
		return false;
	}
	
	private static boolean handleError(Exception e, CommandLineInterface cli, String errorMessage, List<ErrorRemediationOption> options) {
		
		log.error(errorMessage, e);
		
		cli.printError(errorMessage + ": %s", e.getMessage());
		cli.println();
		
		ErrorRemediationOption option = cli.askUserSelection("Decide how to handle the error", options);
		ErrorRemediation remediation = option.getRemediation();
		String message = option.getMessage();
		
		switch(remediation) {
		
			case RETRY:
				cli.println(message);
				cli.println();
				return true;
		
			case SKIP:
				cli.println(message);
				return false;
				
			case STOP:
				throw new BusinessException(message, e);
				
			default:
				throw new IllegalStateException("Unrecognized remediation: " + remediation, e);
		}
	}
}
