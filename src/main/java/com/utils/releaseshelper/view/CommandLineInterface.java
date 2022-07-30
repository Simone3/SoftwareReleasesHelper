package com.utils.releaseshelper.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.view.SelectOption;

import lombok.extern.slf4j.Slf4j;

/**
 * A CLI helper to display and prompt simple command line instructions
 */
@Slf4j
public class CommandLineInterface {

	private static final String GROUP_TITLE_PREFIX = " ----- ";
	private static final String GROUP_INDENTATION  = "|   ";
	private static final String AFTER_PROMPT_SEPARATOR = ". . . . . . . . . . . . . . . . . . . .";
	
	private static final String INPUT_YES = "y";
	private static final String INPUT_NO  = "n";
	private static final String INPUT_ALL = "*";

	private final Scanner scanner;
	
	private String currentIndentation = "";
	
	public CommandLineInterface() {
		
		this.scanner = new Scanner(System.in);
	}
	
	public void print(String message, Object... args) {
		
		System.out.print(currentIndentation + formatMessage(message, args));
	}

	public void println() {
		
		System.out.println(currentIndentation);
	}
	
	public void println(String message, Object... args) {
		
		System.out.println(currentIndentation + formatMessage(message, args));
	}
	
	public void printSeparator() {
		
		printSeparator(true);
	}
	
	public void printSeparator(boolean emptyLines) {
		
		if(emptyLines) {
			
			println();
		}
		println("-------------------------");
		
		if(emptyLines) {
			
			println();
		}
	}
	
	public void printError(String error, Object... args) {
		
		println("[ERROR] " + error, args);
	}
	
	public String getUserInput(String message) {
		
		return getUserInput(message, ":", null);
	}
	
	public String getUserInput(String message, String lastCharacter, String defaultValue) {
		
		String input = null;
		
		while(StringUtils.isBlank(input)) {
			
			String defaultMessage = "";
			if(!StringUtils.isBlank(defaultValue)) {
				
				defaultMessage = " [default: " + defaultValue + "]";
			}
			
			print(message + defaultMessage + lastCharacter + " ");
			
			input = scanner.nextLine();
			
			if(StringUtils.isBlank(input) && !StringUtils.isBlank(defaultValue)) {
				
				input = defaultValue;
			}
		}
		
		println();
		println(AFTER_PROMPT_SEPARATOR);
		println();
		
		return input.trim();
	}
	
	public boolean askUserConfirmation(String message) {
		
		String confirm = null;
		
		while(!INPUT_YES.equalsIgnoreCase(confirm) && !INPUT_NO.equalsIgnoreCase(confirm)) {
			
			confirm = getUserInput(message + " (" + INPUT_YES + "/" + INPUT_NO + ")", "?", null);
		}
		
		return INPUT_YES.equalsIgnoreCase(confirm);
	}
	
	public <T extends SelectOption> T askUserSelection(String message, List<T> options) {
		
		return askUserSelection(message, options, null, false);
	}
	
	public <T extends SelectOption> T askUserSelection(String message, List<T> options, String defaultSelection, boolean forceDefaultSelection) {
		
		return handleUserSelection(message, options, defaultSelection, forceDefaultSelection, false, false).get(0);
	}
	
	public <T extends SelectOption> List<T> askUserSelectionMultiple(String message, List<T> options) {
		
		return askUserSelectionMultiple(message, options, false, null, false);
	}
	
	public <T extends SelectOption> List<T> askUserSelectionMultiple(String message, List<T> options, boolean allowAllSymbol, String defaultSelection, boolean forceDefaultSelection) {
		
		return handleUserSelection(message, options, defaultSelection, forceDefaultSelection, true, allowAllSymbol);
	}
	
	public void startIndentationGroup(String groupTitle, Object... args) {
		
		println();
		println(GROUP_TITLE_PREFIX + groupTitle, args);
		currentIndentation += GROUP_INDENTATION;
		println();
	}
	
	public void endIndentationGroup(String groupTitle, Object... args) {
		
		Assert.isTrue(currentIndentation.length() >= GROUP_INDENTATION.length(), "Trying to end an indentation group without the corresponding start!");
		println();
		currentIndentation = currentIndentation.substring(0, currentIndentation.length() - GROUP_INDENTATION.length());
		println(GROUP_TITLE_PREFIX + groupTitle, args);
		println();
	}
	
	public void resetIndentation() {
		
		currentIndentation = "";
	}
	
	public String formatMessage(String message, Object... args) {
		
		if(args == null || args.length == 0) {
			
			return message;
		}
		else {
			
			return String.format(message, args);
		}
	}
	
	private <T extends SelectOption> List<T> handleUserSelection(String message, List<T> options, String defaultSelection, boolean forceDefaultSelection, boolean multiple, boolean allowAllSymbol) {
		
		if(CollectionUtils.isEmpty(options)) {
			
			throw new IllegalStateException("Unexpected empty options list!");
		}
		else if(forceDefaultSelection) {
			
			if(StringUtils.isBlank(defaultSelection)) {
				
				throw new IllegalStateException("Force default selection but no default value was provided!");
			}
			
			return forcePreFilledOptionSelection(message, options, defaultSelection, multiple, allowAllSymbol);
		}
		else if(options.size() == 1) {
			
			return forceSingleOptionSelection(message, options);
		}
		else {
			
			return promptOptionSelection(message, options, defaultSelection, multiple, allowAllSymbol);
		}
	}
	
	private <T extends SelectOption> List<T> forcePreFilledOptionSelection(String message, List<T> options, String defaultSelection, boolean multiple, boolean allowAllSymbol) {
		
		try {
			
			defaultSelection = defaultSelection.trim();

			println(message + ": pre-filled selection is \"" + defaultSelection + "\"");
			return parseSelectedOptions(defaultSelection, options, multiple, allowAllSymbol);
		}
		catch(Exception e) {
			
			throw new BusinessException(message + ": invalid pre-filled selection \"" + defaultSelection + "\"", e);
		}
	}
	
	private <T extends SelectOption> List<T> forceSingleOptionSelection(String message, List<T> options) {
		
		T onlyOption = options.get(0);
		
		println(message + ": picked only option \"" + onlyOption.getOptionName() + "\"");
		
		return List.of(onlyOption);
	}

	private <T extends SelectOption> List<T> promptOptionSelection(String message, List<T> options, String defaultSelection, boolean multiple, boolean allowAllSymbol) {
		
		int size = options.size();
		
		println(message + ":");
		for(int i = 0; i < size; i++) {
			
			println("  " + (i + 1) + ". " + options.get(i).getOptionName());
		}

		String promptMessage;
		if(multiple) {
			
			String allMessage = allowAllSymbol ? ", " + INPUT_ALL + " for all" : "";
			promptMessage = "Pick one or more options (1-" + size + ", comma separated" + allMessage + ")";
		}
		else {
			
			promptMessage = "Pick one option (1-" + size + ")";
		}

		List<T> pickedOptions = null;
		while(pickedOptions == null) {
		
			String selectionString = getUserInput(promptMessage, ":", defaultSelection);
			
			try {
			
				pickedOptions = parseSelectedOptions(selectionString, options, multiple, allowAllSymbol);
			}
			catch(Exception e) {
				
				log.error("Error parsing option indices", e);
				pickedOptions = null;
			}
		}
		
		return pickedOptions;
	}

	private <T extends SelectOption> List<T> parseSelectedOptions(String selectionString, List<T> options, boolean multiple, boolean allowAllSymbol) {
		
		List<Integer> indices = parseSelectedOptionIndices(selectionString, options.size(), multiple, allowAllSymbol);
		
		List<T> pickedOptions = new ArrayList<>();
		for(int index: indices) {
			
			pickedOptions.add(options.get(index));
		}
		return pickedOptions;
	}

	private List<Integer> parseSelectedOptionIndices(String selectionString, int size, boolean multiple, boolean allowAllSymbol) {
		
		if(multiple && allowAllSymbol && INPUT_ALL.equalsIgnoreCase(selectionString)) {
			
			return IntStream.range(0, size).boxed().collect(Collectors.toList());
		}
		
		List<Integer> indices = new ArrayList<>();
		
		String[] optionStrings = selectionString.split("\\s*,\\s*");
		
		for(String optionString: optionStrings) {
			
			int optionNumber = Integer.parseInt(optionString);
			
			if(optionNumber <= 0 || optionNumber > size) {
				
				throw new BusinessException("Option " + optionNumber + " is out of range");
			}
			
			indices.add(optionNumber - 1);
		}
		
		indices = indices.stream().sorted().distinct().collect(Collectors.toList());
		
		if(!multiple && indices.size() > 1) {
			
			throw new BusinessException("Cannot select more than one option in a non-multiple select");
		}
		
		return indices;
	}
}
