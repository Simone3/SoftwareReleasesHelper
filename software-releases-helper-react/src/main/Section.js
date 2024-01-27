import './Section.css';
import React from 'react';

/**
 * A generic container for "section" of the app, i.e. a component that splits different portions of the app
 */
const Section = ({ children }) => {
	
	return (
		<div className='section'>
			{children}
		</div>
	);
};

export default Section;
