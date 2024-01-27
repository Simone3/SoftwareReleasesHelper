import './HistoryList.css';
import React, { useRef, useEffect } from 'react';
import HistoryRow from './HistoryRow';

/**
 * The list of history messages
 */
const HistoryList = ({ messages }) => {

	const lastElementRef = useRef(null);

	// Effect to scroll towards a newly-added element
	useEffect(() => {
		if(messages.length) {
			lastElementRef.current?.scrollIntoView({
				behavior: 'smooth',
				block: 'end'
			});
		}
	}, [ messages.length ]);

	return (
		<div className='history-list'>
			{messages.map((message, index) => {
				return (
					<HistoryRow
						key={index}
						message={message}
					/>
				);
			})}
			<div ref={lastElementRef} />
		</div>
	);
};

export default HistoryList;
