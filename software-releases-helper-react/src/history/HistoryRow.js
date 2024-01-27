import './HistoryRow.css';
import React from 'react';

/**
 * An element of the history list
 */
const HistoryRow = ({ message }) => {
	return (
		<div className={`history-element history-element-${message.type.toLowerCase()}`}>
			<div className='history-element-date'>{message.formattedDate}</div>
			<div className='history-element-message' dangerouslySetInnerHTML={{ __html: message.text }} />
		</div>
	);
};

export default HistoryRow;
