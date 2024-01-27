import HistoryList from './HistoryList';
import Section from '../main/Section';
import Area from '../main/Area';

/**
 * The history section
 */
const HistorySection = ({ messages }) => {
	
	return (
		<Section>
			<Area title={'History'}>
				<HistoryList
					messages={messages}
				/>
			</Area>
		</Section>
	);
};

export default HistorySection;
