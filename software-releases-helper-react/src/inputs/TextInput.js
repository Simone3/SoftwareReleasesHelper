import './TextInput.css';
import React from 'react';

/**
 * A text input
 */
const TextInput = ({ enabled, value, onChange }) => {

	return (
		<div className='text-input'>
			<div className='text-input-container'>
				<input
					className='text-input-value'
					disabled={!enabled}
					type='text'
					autoCorrect='off'
					autoCapitalize='none'
					spellCheck='false'
					autoComplete='off'
					placeholder='Type value...'
					value={value || ''}
					onChange={(e) => {
						onChange(e.target.value);
					}}
				/>
			</div>
		</div>
	);
};

export default TextInput;

