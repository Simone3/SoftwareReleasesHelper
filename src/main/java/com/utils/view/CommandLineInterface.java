package com.utils.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.utils.model.view.SelectOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandLineInterface {
	
	private static final String INPUT_YES = "y";
	private static final String INPUT_NO  = "n";
	private static final String INPUT_ALL = "*";

	private final Scanner scanner;
	
	public CommandLineInterface() {
		
		this.scanner = new Scanner(System.in);
	}
	
	public void print(String message, Object... args) {
		
		System.out.print(formatMessage(message, args));
	}

	public void println() {
		
		System.out.println();
	}
	
	public void println(String message, Object... args) {
		
		System.out.println(formatMessage(message, args));
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
	
	public void printTitle(String title) {
		
		println();
		println("********* " + title + " *********");
		println();
	}
	
	public void printError(String error, Object... args) {
		
		println("[ERROR] " + error, args);
	}
	
	public String getUserInput(String message, Object... args) {
		
		String input = null;
		
		while(StringUtils.isBlank(input)) {
			
			print(formatMessage(message, args));
			input = scanner.nextLine();
		}
		
		return input.trim();
	}
	
	public boolean askUserConfirmation(String message, Object... args) {
		
		String confirm = null;
		
		while(!INPUT_YES.equalsIgnoreCase(confirm) && !INPUT_NO.equalsIgnoreCase(confirm)) {
			
			confirm = getUserInput(formatMessage(message, args) + " (" + INPUT_YES + "/" + INPUT_NO + ")? ");
		}
		
		return INPUT_YES.equalsIgnoreCase(confirm);
	}
	
	public <T extends SelectOption> T askUserSelection(String message, List<T> options) {
		
		return askUserSelection(message, options, null);
	}
	
	public <T extends SelectOption> T askUserSelection(String message, List<T> options, String optionalPreFilledSelection) {
		
		return handleUserSelection(message, options, optionalPreFilledSelection, false, false).get(0);
	}
	
	public <T extends SelectOption> List<T> askUserSelectionMultiple(String message, List<T> options) {
		
		return askUserSelectionMultiple(message, options, false, null);
	}
	
	public <T extends SelectOption> List<T> askUserSelectionMultiple(String message, List<T> options, boolean allowAllSymbol, String optionalPreFilledSelection) {
		
		return handleUserSelection(message, options, optionalPreFilledSelection, true, allowAllSymbol);
	}
	
	private String formatMessage(String message, Object... args) {
		
		if(args == null || args.length == 0) {
			
			return message;
		}
		else {
			
			return String.format(message, args);
		}
	}
	
	private <T extends SelectOption> List<T> handleUserSelection(String message, List<T> options, String optionalPreFilledSelection, boolean multiple, boolean allowAllSymbol) {
		
		if(options == null || options.isEmpty()) {
			
			throw new IllegalStateException("Unexpected empty options list!");
		}
		else if(!StringUtils.isBlank(optionalPreFilledSelection)) {
			
			return forcePreFilledOptionSelection(message, options, optionalPreFilledSelection, multiple, allowAllSymbol);
		}
		else if(options.size() == 1) {
			
			return forceSingleOptionSelection(message, options);
		}
		else {
			
			return promptOptionSelection(message, options, multiple, allowAllSymbol);
		}
	}
	
	private <T extends SelectOption> List<T> forcePreFilledOptionSelection(String message, List<T> options, String optionalPreFilledSelection, boolean multiple, boolean allowAllSymbol) {
		
		try {
			
			optionalPreFilledSelection = optionalPreFilledSelection.trim();

			println(message + ": pre-filled selection is \"" + optionalPreFilledSelection + "\"");
			return parseSelectedOptions(optionalPreFilledSelection, options, multiple, allowAllSymbol);
		}
		catch(Exception e) {
			
			throw new IllegalStateException(message + ": invalid pre-filled selection \"" + optionalPreFilledSelection + "\"");
		}
	}
	
	private <T extends SelectOption> List<T> forceSingleOptionSelection(String message, List<T> options) {
		
		T onlyOption = options.get(0);
		
		println(message + ": picked only option \"" + onlyOption.getOptionName() + "\"");
		
		return List.of(onlyOption);
	}

	private <T extends SelectOption> List<T> promptOptionSelection(String message, List<T> options, boolean multiple, boolean allowAllSymbol) {
		
		var size = options.size();
		
		println(message + ":");
		for(var i = 0; i < size; i++) {
			
			println("  " + (i + 1) + ". " + options.get(i).getOptionName());
		}
		
		String promptMessage;
		if(multiple) {
			
			var allMessage = allowAllSymbol ? ", " + INPUT_ALL + " for all" : "";
			promptMessage = "Pick one or more options (1-" + size + ", comma separated" + allMessage + "): ";
		}
		else {
			
			promptMessage = "Pick one option (1-" + size + "): ";
		}

		List<T> pickedOptions = null;
		while(pickedOptions == null) {
		
			var selectionString = getUserInput(promptMessage);
			
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
		
		var indices = parseSelectedOptionIndices(selectionString, options.size(), multiple, allowAllSymbol);
		
		List<T> pickedOptions = new ArrayList<>();
		for(var index: indices) {
			
			pickedOptions.add(options.get(index));
		}
		return pickedOptions;
	}

	private List<Integer> parseSelectedOptionIndices(String selectionString, int size, boolean multiple, boolean allowAllSymbol) {
		
		if(multiple && allowAllSymbol && INPUT_ALL.equalsIgnoreCase(selectionString)) {
			
			return IntStream.range(0, size).boxed().collect(Collectors.toList());
		}
		
		List<Integer> indices = new ArrayList<>();
		
		var optionStrings = selectionString.split("\\s*,\\s*");
		
		for(var optionString: optionStrings) {
			
			var optionNumber = Integer.valueOf(optionString);
			
			if(optionNumber <= 0 || optionNumber > size) {
				
				throw new IllegalStateException("Option " + optionNumber + " is out of range");
			}
			
			indices.add(optionNumber - 1);
		}
		
		indices = indices.stream().sorted().distinct().collect(Collectors.toList());
		
		if(!multiple && indices.size() > 1) {
			
			throw new IllegalStateException("Cannot select more than one option in a non-multiple select");
		}
		
		return indices;
	}
}
