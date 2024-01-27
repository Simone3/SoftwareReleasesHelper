import './ActionsList.css';
import React from 'react';
import ActionElement from './ActionElement';

/**
 * The actions list
 */
const ActionsList = ({ actions, onSelectAction }) => {

	return (
		<div className='actions-list'>
			{actions.map((action) => {
				return (
					<div key={action.name}>
						<ActionElement
							action={action}
							onClick={() => {
								onSelectAction(action);
							}}
						/>
					</div>
				);
			})}
		</div>
	);
};

export default ActionsList;
