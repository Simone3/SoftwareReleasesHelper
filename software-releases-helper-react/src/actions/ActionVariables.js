import './ActionVariables.css';
import React from 'react';
import FreeSelectInput from '../inputs/FreeSelectInput';
import TextInput from '../inputs/TextInput';
import StrictSelectInput from '../inputs/StrictSelectInput';

/**
 * The action variables for the details section
 */
const ActionVariables = ({ executionStatus, action, valuesMap, updateVariableValue }) => {

	const inputsEnabled = executionStatus === 'USER_INPUT';
	
	// Helper function to render the different types of variables
	const renderVariable = (variable) => {
		switch(variable.type) {

			case 'FREE_SELECT':
				return (
					<FreeSelectInput
						enabled={inputsEnabled}
						options={variable.options}
						value={valuesMap[variable.key]}
						onChange={(value) => updateVariableValue(variable, value)}
					/>
				);

			case 'STRICT_SELECT':
				return (
					<StrictSelectInput
						enabled={inputsEnabled}
						options={variable.options}
						value={valuesMap[variable.key]}
						onChange={(value) => updateVariableValue(variable, value)}
					/>
				);
			
			case 'STATIC':
				return (
					<TextInput
						enabled={false}
						value={valuesMap[variable.key]}
					/>
				);
			
			case 'TEXT':
				return (
					<TextInput
						enabled={inputsEnabled}
						value={valuesMap[variable.key]}
						onChange={(value) => updateVariableValue(variable, value)}
					/>
				);
			
			default:
				console.error(`Unknown variable type: ${variable.type}`);
				return null;
		}
	};

	return (
		<div className='action-variables'>
			Define these values to replace the placeholders:
			<div className='inputs-container'>
				{action.variables.map((variable) => {
					return (
						<div key={variable.key} className='input-container'>
							<div className='input-label'>{variable.key}</div>
							{renderVariable(variable)}
						</div>
					);
				})}
			</div>
		</div>
	);
};

export default ActionVariables;
