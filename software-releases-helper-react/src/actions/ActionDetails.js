import './ActionDetails.css';
import React, { useState } from 'react';
import ActionVariables from './ActionVariables';
import ActionControlPanel from './ActionControlPanel';
import ActionDescription from './ActionDescription';
import Utils from '../utils/Utils';

/**
 * The action details section content
 */
const ActionDetails = ({ action, executionStatus, onStartAction, onResumeAction, onCancelAction }) => {
	
	// Variable values state (used both for actual input values and placeholder replacement)
	const [ variablesState, setVariablesState ] = useState(() => {
		if(!action.variables) {
			return {
				allDefined: true
			};
		}

		const valuesMap = {};
		const definedValuesList = [];
		for(const variable of action.variables) {
			const key = variable.key;
			let value = variable.value;
			if(value && variable.removeWhitespace) {
				value = Utils.removeWhitespace(value);
			}

			if(value) {
				valuesMap[key] = value;
				definedValuesList.push({ key: key, value: value });
			}
			else {
				valuesMap[key] = undefined;
			}
		}

		return {
			valuesMap: valuesMap,
			definedValuesList: definedValuesList,
			allDefined: action.variables.length === definedValuesList.length
		};
	});

	// Helper function to update the state
	const updateVariableValue = (variable, newValue) => {
		setVariablesState((oldVariablesState) => {
			if(newValue && variable.removeWhitespace) {
				newValue = Utils.removeWhitespace(newValue);
			}

			const key = variable.key;
			const newValuesMap = { ...oldVariablesState.valuesMap };
			if(newValue) {
				newValuesMap[key] = newValue;
			}
			else {
				delete newValuesMap[key];
			}

			const newDefinedValuesList = [];
			for(const loopVariable of action.variables) {
				const loopKey = loopVariable.key;
				const loopValue = newValuesMap[loopKey];
				if(loopValue) {
					newDefinedValuesList.push({ key: loopKey, value: loopValue });
				}
			}

			return {
				valuesMap: newValuesMap,
				definedValuesList: newDefinedValuesList,
				allDefined: action.variables.length === newDefinedValuesList.length
			};
		});
	};

	return (
		<div className='action-details-container'>
			<ActionDescription
				action={action}
				variableValues={variablesState.definedValuesList}
			/>
			{action.variables && action.variables.length > 0 &&
				<ActionVariables
					executionStatus={executionStatus}
					action={action}
					valuesMap={variablesState.valuesMap}
					updateVariableValue={updateVariableValue}
				/>
			}
			<ActionControlPanel
				executionStatus={executionStatus}
				allVariablesDefined={variablesState.allDefined}
				variableValues={variablesState.definedValuesList}
				onStartAction={onStartAction}
				onResumeAction={onResumeAction}
				onCancelAction={onCancelAction}
			/>
		</div>
	);
};

export default ActionDetails;
