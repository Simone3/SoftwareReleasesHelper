import './Overlay.css';
import React, { useState, useEffect } from 'react';

const delayMilliseconds = 500;

/**
 * An overlay component for static messages
 */
const Overlay = ({ backEndConnected }) => {

	const [ isShown, setIsShown ] = useState(false);

	// Effect to delay for half a second the "back-end disconnected" message
	useEffect(() => {
		if(!backEndConnected) {
			const timer = setTimeout(() => {
				setIsShown(true);
			}, delayMilliseconds);
			return () => clearTimeout(timer);
		}
	}, [ backEndConnected ]);

	if(backEndConnected) {
		return null;
	}
	else if(!isShown) {
		return null;
	}
	else {
		return (
			<div className='disconnected-error-container'>
				<div className='disconnected-error'>
					&#9888;<div className='disconnected-error-tooltip'> Back-End server disconnected</div>
				</div>
			</div>
		);
	}
};

export default Overlay;

