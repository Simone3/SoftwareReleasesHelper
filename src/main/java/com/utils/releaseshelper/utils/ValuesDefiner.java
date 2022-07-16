package com.utils.releaseshelper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Helper class that allows to:
 * - register some value definitions
 * - process all "ask me" value definitions together
 * - get values (for "ask me" definitions: before processing the placeholders, after processing the actual values)
 */
public class ValuesDefiner {
	
	private final CommandLineInterface cli;
	private final Map<String, String> sourceVariables;

	private int currentAskMeCounter = 0;
	private boolean defined = false;
	private final List<ValueDefinition> askMeValueDefinitions = new ArrayList<>();
	private final Map<ValueDefinition, ValueResult> valuesMap = new HashMap<>();
	
	public ValuesDefiner(CommandLineInterface cli, Map<String, String> sourceVariables) {
		
		this.cli = cli;
		this.sourceVariables = sourceVariables;
	}

	public void addValueDefinition(ValueDefinition valueDefinition, String valueNickname) {
		
		Assert.isTrue(!defined, "Cannot add new values when values have been defined!");
		
		if(valueDefinition.isAskMe()) {
			
			askMeValueDefinitions.add(valueDefinition);
			valuesMap.put(valueDefinition, new ValueResult(false, "{V" + currentAskMeCounter + "}", valueNickname, null));
			currentAskMeCounter++;
		}
		else {
			
			String value = VariablesUtils.defineValue(cli, null, valueDefinition, sourceVariables);
			valuesMap.put(valueDefinition, new ValueResult(true, null, null, value));
		}
	}
	
	public boolean hasAskMeValues() {
		
		return !askMeValueDefinitions.isEmpty();
	}
	
	public void defineAskMeValues() {
		
		Assert.isTrue(!defined, "Cannot define values twice!");
		
		defined = true;
		
		for(ValueDefinition valueDefinition: askMeValueDefinitions) {
			
			ValueResult result = valuesMap.get(valueDefinition);
			
			String prompt = "Define value " + result.getAskMeId() + "(" + result.getNickname() + ")";
			String value = VariablesUtils.defineValue(cli, prompt, valueDefinition, sourceVariables);
			
			result.setProcessed(true);
			result.setValue(value);
		}
	}
	
	public String getValue(ValueDefinition valueDefinition) {
		
		ValueResult result = valuesMap.get(valueDefinition);
		Assert.notNull(result, "Value definition is not recognized, was it previously registered with addValueDefinition()?");
		
		if(result.isProcessed()) {
			
			return result.getValue();
		}
		else {
			
			return result.getAskMeId();
		}
	}
	
	@Data
	@AllArgsConstructor
	@RequiredArgsConstructor
	private static class ValueResult {
		
		private boolean processed;
		private String askMeId;
		private String nickname;
		private String value;
	}
}
