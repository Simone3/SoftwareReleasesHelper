package com.utils.releaseshelper.view.userinterface;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.model.view.cli.ParseResult;
import com.utils.releaseshelper.model.view.cli.SelectOption;
import com.utils.releaseshelper.utils.MiscUtils;

/**
 * The CLI utils
 */
@Component
public class CommandLineInterface extends UserInterface {
	
	private static final String AFTER_PROMPT_SEPARATOR = ". . . . . . . . . . . . . . . . . . . .";
	
	private final Scanner scanner;
	
	public CommandLineInterface() {
		
		this.scanner = new Scanner(System.in);
	}
	
	@Override
	protected void doClearCurrentData() {
		
		// Do nothing here for now
	}
	
	@Override
	protected void doPrintLine() {
		
		System.out.println();
	}
	
	@Override
	protected void doPrintLine(String message) {
		
		System.out.println(message);
	}

	private void doPrint(String message, Object... args) {
		
		System.out.print(currentIndentation + MiscUtils.formatMessage(message, args));
	}
	
	@Override
	protected String doGetTextInput(String message, String lastCharacter, String defaultValue) {
		
		ParseResult<String> result;
		
		while(true) {
			
			String defaultMessage = "";
			if(!StringUtils.isBlank(defaultValue)) {
				
				defaultMessage = " [default: " + defaultValue + "]";
			}
			
			doPrint(message + defaultMessage + lastCharacter + " ");
			
			String input = scanner.nextLine();
			
			if(StringUtils.isBlank(input) && !StringUtils.isBlank(defaultValue)) {
				
				input = defaultValue;
			}
			
			result = parseTextInput(input);
			
			if(result.isSuccess()) {
				
				break;
			}
			else {
				
				printError(result.getMessage());
			}
		}

		doPrintLine();
		doPrintLine(AFTER_PROMPT_SEPARATOR);
		doPrintLine();
		
		return result.getValue();
	}
	
	@Override
	protected boolean doGetBooleanInput(String message) {
		
		ParseResult<Boolean> result;
		
		while(true) {
			
			String input = doGetTextInput(message + " (" + INPUT_YES + "/" + INPUT_NO + ")", "?", null);
			result = parseBooleanInput(input);
			
			if(result.isSuccess()) {
				
				break;
			}
			else {
				
				printError(result.getMessage());
			}
		}
		
		return result.getValue();
	}

	@Override
	protected <T extends SelectOption> List<T> doGetSelectInput(String message, List<T> options, String defaultSelection, boolean multiple, boolean allowAllSymbol) {
		
		int size = options.size();
		
		doPrintLine(message + ":");
		for(int i = 0; i < size; i++) {
			
			doPrintLine("  " + (i + 1) + ". " + options.get(i).getOptionName());
		}

		String promptMessage;
		if(multiple) {
			
			String allMessage = allowAllSymbol ? ", " + INPUT_ALL + " for all" : "";
			promptMessage = "Pick one or more options (1-" + size + ", comma separated" + allMessage + ")";
		}
		else {
			
			promptMessage = "Pick one option (1-" + size + ")";
		}

		ParseResult<List<T>> result;
		while(true) {
		
			String selectionString = doGetTextInput(promptMessage, ":", defaultSelection);
			
			result = parseSelectInput(selectionString, options, multiple, allowAllSymbol);
			
			if(result.isSuccess()) {
				
				break;
			}
			else {
				
				printError(result.getMessage());
			}
		}
		
		return result.getValue();
	}
}
