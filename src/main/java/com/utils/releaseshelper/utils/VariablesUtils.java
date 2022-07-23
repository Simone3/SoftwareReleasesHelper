package com.utils.releaseshelper.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.ValueDefinitionType;
import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.view.SelectOption;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.experimental.UtilityClass;

/**
 * Helper util to define variable definitions logic
 */
@UtilityClass
public class VariablesUtils {
	
	public static String defineValue(CommandLineInterface cli, String promptPrefix, ValueDefinition valueToDefine, Map<String, String> sourceVariables) {
		
		boolean manuallyDefine = valueToDefine.isAskMe();
		ValueDefinitionType definitionType = valueToDefine.getDefinitionType();
		boolean removeWhitespace = valueToDefine.isRemoveWhitespace();
		String staticValue = valueToDefine.getStaticContent();
		List<SelectOption> options = valueToDefine.getOptions();
		
		String value;
		if(manuallyDefine) {
			
			String whitespaceNote = "";
			String defaultValue = null;
			
			if(removeWhitespace) {
				
				whitespaceNote = " (all spaces will be removed)";
			}
			
			if(!StringUtils.isBlank(staticValue)) {
				
				defaultValue = staticValue;
			}
			
			String message = cli.formatMessage("%s%s", promptPrefix, whitespaceNote);
			
			if(definitionType == ValueDefinitionType.DYNAMIC_SELECT) {
				
				value = cli.askUserSelection(message, options, defaultValue, false).getOptionName();
			}
			else {
				
				value = cli.getUserInput(message, ":", defaultValue);
			}
			
		}
		else {
			
			value = replaceVariablePlaceholders(staticValue, sourceVariables, null);
		}
		
		if(removeWhitespace) {
			
			value = value.replaceAll("\\s+", "");
			value = value.replace("\u200B", "");
		}

		return value;
	}

	public static String defineVariable(CommandLineInterface cli, String promptPrefix, VariableDefinition variableToDefine, Map<String, String> sourceVariables) {
		
		String key = variableToDefine.getKey();
		return defineValue(cli, promptPrefix + " \"" + key + "\"", variableToDefine.getValueDefinition(), sourceVariables);
	}
	
	public static String replaceVariablePlaceholders(String sourceString, Map<String, String> variables, Map<String, String> customPlaceholders) {
		
		if(StringUtils.isBlank(sourceString)) {
			
			return sourceString;
		}
		
		Map<String, String> substitutorVariables = new HashMap<>();
		
		if(!CollectionUtils.isEmpty(variables)) {
			
			substitutorVariables.putAll(variables);
		}
		
		if(!CollectionUtils.isEmpty(customPlaceholders)) {
			
			substitutorVariables.putAll(customPlaceholders);
		}
		
		if(substitutorVariables.isEmpty()) {
			
			return sourceString;
		}
		
		StringSubstitutor substitutor = new StringSubstitutor(substitutorVariables, "#[", "]", '#');
		return substitutor.replace(sourceString);
	}
}
