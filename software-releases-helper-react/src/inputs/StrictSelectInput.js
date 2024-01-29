import './StrictSelectInput.css';
import React, { useState } from 'react';
import { useDetectClickOutside } from 'react-detect-click-outside';

/**
 * A dropdown input
 */
const StrictSelectInput = ({ options, enabled, value, onChange }) => {

	const [ state, setState ] = useState({
		open: false,
		filter: ''
	});

	const ref = useDetectClickOutside({
		onTriggered: () => {
			if(state.open) {
				setState(() => {
					return {
						open: false,
						filter: ''
					};
				});
			}
		}
	});

	const onInputClick = enabled ?
		() => {
			setState(() => {
				return {
					open: !state.open,
					filter: ''
				};
			});
		} :
		undefined;

	const filteredOptions = state.filter ?
		options.filter((option) => {
			return option.toLowerCase().indexOf(state.filter.toLowerCase()) !== -1;
		}) :
		options;
	
	return (
		<div className={`strict-select-input strict-select-input-${state.open ? 'open' : 'closed'} strict-select-input-${enabled ? 'enabled' : 'disabled'}`} ref={ref}>
			<div
				className='strict-select-input-fixed-container'
				onClick={onInputClick}>
				<div
					className={`strict-select-input-value${value ? '' : ' strict-select-input-value-placeholder'}`}>
					{value ? value : 'Type or select value...'}
				</div>
				<div className='strict-select-input-arrow-container'>
					<div>
						<span className='strict-select-input-arrow'></span>
					</div>
				</div>
			</div>
			<div className='strict-select-input-dropdown-container'>
				<div className='strict-select-input-search-container'>
					<input
						className='strict-select-input-search'
						type='search'
						autoCorrect='off'
						autoCapitalize='none'
						spellCheck='false'
						autoComplete='off'
						placeholder='Filter...'
						value={state.filter}
						onChange={(e) => {
							setState(() => {
								return {
									open: true,
									filter: e.target.value
								};
							});
						}}
					/>
				</div>
				<div className='strict-select-input-options-container'>
					{filteredOptions.length > 0 ?
						<ul className='strict-select-input-options'>
							{filteredOptions.map((option, index) => {
								return (
									<li
										key={index}
										className='strict-select-input-option'
										onClick={() => {
											setState(() => {
												return {
													open: false,
													filter: ''
												};
											});
											onChange(option);
										}}>
										{option}
									</li>
								);
							})}
						</ul> :
						<div className='strict-select-input-options-none'>No options</div>
					}
				</div>
			</div>
		</div>
	);
};

export default StrictSelectInput;

