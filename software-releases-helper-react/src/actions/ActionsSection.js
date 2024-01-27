import Loader from '../main/Loader';
import ActionsList from './ActionsList';
import ActionDetails from './ActionDetails';
import Section from '../main/Section';
import Area from '../main/Area';

/**
 * The dynamic actions section (either loading or list or details sections)
 */
const ActionsSection = ({ domainInitialized, actions, currentAction, executionStatus, onSelectAction, onExitAction, onStartAction, onResumeAction, onCancelAction }) => {
	
	if(!domainInitialized) {
		return (
			<Section>
				<Area title={'Select an action'}>
					<Loader />
				</Area>
			</Section>
		);
	}

	if(!currentAction) {
		return (
			<Section>
				<Area title={'Select an action'}>
					<ActionsList
						actions={actions}
						onSelectAction={onSelectAction}
					/>
				</Area>
			</Section>
		);
	}

	return (
		<Section>
			<Area title={currentAction.name} backVisible={true} backEnabled={executionStatus === 'USER_INPUT'} onBack={onExitAction}>
				<ActionDetails
					action={currentAction}
					executionStatus={executionStatus}
					onStartAction={onStartAction}
					onResumeAction={onResumeAction}
					onCancelAction={onCancelAction}
				/>
			</Area>
		</Section>
	);
};

export default ActionsSection;
