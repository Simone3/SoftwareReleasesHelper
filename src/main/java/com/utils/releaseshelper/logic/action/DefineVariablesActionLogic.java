package com.utils.releaseshelper.logic.action;

import java.util.Map;

import com.utils.releaseshelper.model.logic.VariableDefinition;
import com.utils.releaseshelper.model.logic.action.DefineVariablesAction;
import com.utils.releaseshelper.utils.ValuesDefiner;
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to define one or more variables
 */
public class DefineVariablesActionLogic extends ActionLogic<DefineVariablesAction> {

	public DefineVariablesActionLogic(DefineVariablesAction action, Map<String, String> variables, CommandLineInterface cli) {
		
		super(action, variables, cli);
	}

	@Override
	protected void beforeAction() {
		
		for(VariableDefinition variableToDefine: action.getVariables()) {
			
			String value = VariablesUtils.defineVariable(cli, "Define variable", variableToDefine, variables);
			variables.put(variableToDefine.getKey(), value);
		}
		
		cli.println("All variables defined");
	}

	@Override
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {
		
		// Do nothing here for now
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		// Do nothing here for now
	}

	@Override
	protected void doRunAction(ValuesDefiner valuesDefiner) {
		
		// Do nothing here for now
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
}
