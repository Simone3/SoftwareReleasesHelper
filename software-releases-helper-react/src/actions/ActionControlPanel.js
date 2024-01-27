import './ActionControlPanel.css';
import React from 'react';

/**
 * The action buttons for the details section
 */
const ActionControlPanel = ({ executionStatus, allVariablesDefined, variableValues, onStartAction, onResumeAction, onCancelAction }) => {
		
	switch(executionStatus) {
		case 'USER_INPUT':
			return (
				<div className='action-buttons'>
					<button
						className='action-button action-button-start'
						type='button'
						disabled={!allVariablesDefined}
						onClick={() => {
							onStartAction(variableValues);
						}}>
						Start
					</button>
				</div>
			);
		
		case 'EXECUTING_RUN':
			return (
				<div className='action-buttons'>
					<button
						className='action-button action-button-start'
						disabled={true}>
						Start <span className='loader'></span>
					</button>
				</div>
			);

		case 'SUSPENDED':
			return (
				<div className='action-buttons'>
					<button
						className='action-button action-button-resume'
						disabled={false}
						type='button'
						onClick={() => {
							onResumeAction();
						}}>
						Resume
					</button>
					<button
						className='action-button action-button-cancel'
						disabled={false}
						type='button'
						onClick={() => {
							onCancelAction();
						}}>
						Cancel
					</button>
				</div>
			);
		
		case 'EXECUTING_RESUME':
			return (
				<div className='action-buttons'>
					<button
						className='action-button action-button-resume'
						disabled={true}>
						Resume <span className='loader'></span>
					</button>
					<button
						className='action-button action-button-cancel'
						disabled={true}>
						Cancel
					</button>
				</div>
			);

		default:
			console.error(`Unmapped buttons for execution state ${executionStatus}`);
			return null;
	}
};

export default ActionControlPanel;
