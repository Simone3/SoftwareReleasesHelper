package com.utils.releaseshelper.view.userinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.view.cli.ParseResult;
import com.utils.releaseshelper.model.view.cli.SelectOption;
import com.utils.releaseshelper.utils.MiscUtils;

import lombok.RequiredArgsConstructor;

/**
 * Generic UI logic
 */
@RequiredArgsConstructor
public abstract class UserInterface {

	protected static final String INPUT_YES = "y";
	protected static final String INPUT_NO  = "n";
	protected static final String INPUT_ALL = "*";
	
	private static final String GROUP_TITLE_PREFIX = " ----- ";
	private static final String GROUP_INDENTATION  = "|   ";
	
	protected String currentIndentation = "";
	
	public void clearCurrentData() {
		
		resetIndentation();
		doClearCurrentData();
	}

	public void printLine() {
		
		doPrintLine(currentIndentation);
	}

	public void printLine(String message, Object... args) {
		
		doPrintLine(currentIndentation + MiscUtils.formatMessage(message, args));
	}

	public void printError(String error, Object... args) {
		
		printLine("[ERROR] " + error, args);
	}

	public String getUserInput(String message) {
		
		return doGetTextInput(message, ":", null);
	}

	public String getUserInput(String message, String lastCharacter, String defaultValue) {
		
		return doGetTextInput(message, lastCharacter, defaultValue);
	}

	public boolean askUserConfirmation(String message) {
		
		return doGetBooleanInput(message);
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
	
	public void printSeparator() {
		
		printSeparator(true);
	}

	public void startIndentationGroup(String groupTitle, Object... args) {
		
		printLine();
		printLine(GROUP_TITLE_PREFIX + groupTitle, args);
		currentIndentation = currentIndentation + GROUP_INDENTATION;
		printLine();
	}

	public void endIndentationGroup(String groupTitle, Object... args) {
		
		Assert.isTrue(currentIndentation.length() >= GROUP_INDENTATION.length(), "Trying to end an indentation group without the corresponding start!");
		printLine();
		currentIndentation = currentIndentation.substring(0, currentIndentation.length() - GROUP_INDENTATION.length());
		printLine(GROUP_TITLE_PREFIX + groupTitle, args);
		printLine();
	}
	
	protected abstract void doClearCurrentData();

	protected abstract void doPrintLine();
	
	protected abstract void doPrintLine(String message);
	
	protected abstract String doGetTextInput(String message, String lastCharacter, String defaultValue);
	
	protected abstract boolean doGetBooleanInput(String message);

	protected abstract <T extends SelectOption> List<T> doGetSelectInput(String message, List<T> options, String defaultSelection, boolean multiple, boolean allowAllSymbol);
	
	protected ParseResult<String> parseTextInput(String value) {
		
		if(StringUtils.isBlank(value)) {
			
			return ParseResult.fail("Value cannot be empty");
		}
		else {
			
			return ParseResult.ok(value.trim());
		}
	}
	
	protected ParseResult<Boolean> parseBooleanInput(String value) {
		
		if(!INPUT_YES.equalsIgnoreCase(value) && !INPUT_NO.equalsIgnoreCase(value)) {
			
			return ParseResult.fail("Value must be equal to " + INPUT_YES + " or " + INPUT_NO);
		}
		else {
			
			return ParseResult.ok(INPUT_YES.equalsIgnoreCase(value));
		}
	}
	
	protected <T extends SelectOption> ParseResult<List<T>> parseSelectInput(String value, List<T> options, boolean multiple, boolean allowAllSymbol) {
		
		ParseResult<List<Integer>> parseResult = parseSelectInputIndices(value, options.size(), multiple, allowAllSymbol);
		if(!parseResult.isSuccess()) {
			
			return ParseResult.fail(parseResult.getMessage());
		}
		
		List<T> pickedOptions = new ArrayList<>();
		for(int index: parseResult.getValue()) {
			
			pickedOptions.add(options.get(index));
		}
		return ParseResult.ok(pickedOptions);
	}
	
	private void resetIndentation() {
		
		currentIndentation = "";
	}
	
	private void printSeparator(boolean emptyLines) {
		
		if(emptyLines) {
			
			printLine();
		}
		printLine("-------------------------");
		
		if(emptyLines) {
			
			printLine();
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
			
			return doGetSelectInput(message, options, defaultSelection, multiple, allowAllSymbol);
		}
	}
	
	private <T extends SelectOption> List<T> forcePreFilledOptionSelection(String message, List<T> options, String defaultSelection, boolean multiple, boolean allowAllSymbol) {
		
		defaultSelection = defaultSelection.trim();

		printLine(message + ": pre-filled selection is \"" + defaultSelection + "\"");
		ParseResult<List<T>> result = parseSelectInput(defaultSelection, options, multiple, allowAllSymbol);
		if(result.isSuccess()) {
			
			return result.getValue();
		}
		else {
			
			throw new BusinessException(message + ": invalid pre-filled selection \"" + defaultSelection + "\"");
		}
	}
	
	private <T extends SelectOption> List<T> forceSingleOptionSelection(String message, List<T> options) {
		
		T onlyOption = options.get(0);
		
		printLine(message + ": picked only option \"" + onlyOption.getOptionName() + "\"");
		
		return List.of(onlyOption);
	}

	private ParseResult<List<Integer>> parseSelectInputIndices(String value, int size, boolean multiple, boolean allowAllSymbol) {
		
		if(multiple && allowAllSymbol && INPUT_ALL.equalsIgnoreCase(value)) {
			
			return ParseResult.ok(IntStream.range(0, size).boxed().collect(Collectors.toList()));
		}
		
		List<Integer> indices = new ArrayList<>();
		
		String[] optionStrings = value.split("\\s*,\\s*");
		
		for(String optionString: optionStrings) {
			
			int optionNumber = Integer.parseInt(optionString);
			
			if(optionNumber <= 0 || optionNumber > size) {
				
				return ParseResult.fail("Option " + optionNumber + " is out of range");
			}
			
			indices.add(optionNumber - 1);
		}
		
		indices = indices.stream().sorted().distinct().collect(Collectors.toList());
		
		if(!multiple && indices.size() > 1) {
			
			return ParseResult.fail("Cannot select more than one option in a non-multiple select");
		}
		
		return ParseResult.ok(indices);
	}
}
