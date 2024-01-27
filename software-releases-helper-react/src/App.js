import './App.css';

import React from 'react';
import { StompSessionProvider } from 'react-stomp-hooks';
import MainPage from './main/MainPage';
import WebSocketConstants from './utils/WebSocketConstants';

/**
 * The app entry point
 */
const App = () => {
	return (
		<StompSessionProvider url={WebSocketConstants.webSocketUrl}>
			<MainPage/>
		</StompSessionProvider>
	);
};

export default App;
