import './Area.css';
import React from 'react';

/**
 * A generic container for an "area" of the app, i.e. a portion of content with a title
 */
const Area = ({ children, title, backVisible, backEnabled, onBack }) => {
	
	return (
		<div className='area'>
			<div className='area-title-container'>
				{backVisible &&
					<button
						className='area-title-button area-title-button-back'
						type='button'
						disabled={!backEnabled}
						onClick={onBack}>
						❮
					</button>}
				<h1 className='area-title'>{title}</h1>
			</div>
			<div className='area-content'>
				{children}
			</div>
		</div>
	);
};

export default Area;
