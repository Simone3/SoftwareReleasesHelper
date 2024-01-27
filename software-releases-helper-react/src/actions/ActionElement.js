import './ActionElement.css';
import React from 'react';

/**
 * An element in the actions list section
 */
const ActionElement = ({ action, onClick }) => {

	return (
		<div
			className='action-list-element'
			onClick={onClick}>
			<div className='action-name'>{action.name}</div>
			<div className='action-type'>{action.typeDescription}</div>
		</div>
	);
};

export default ActionElement;
