import './MainPage.css';
import React, { useState, useCallback, useEffect } from 'react';
import { v4 as uuidv4 } from 'uuid';
import { useStompClient, useSubscription } from 'react-stomp-hooks';
import Overlay from './Overlay';
import ActionsSection from '../actions/ActionsSection';
import HistorySection from '../history/HistorySection';
import Utils from './../utils/Utils';
import WebSocketConstants from './../utils/WebSocketConstants';

/**
 * The app main page, i.e. the entry point for everything, the component that holds the centralized state
 * and the component that sends/receives WebSocket events
 */
const MainPage = () => {

	// Application domain state
	const [ domainState, setDomainState ] = useState({
		sessionId: uuidv4(),
		domainInitialized: false,
		actions: []
	});

	// Current action execution state
	const [ actionExecutionState, setActionExecutionState ] = useState({
		executionId: undefined,
		currentAction: undefined,
		status: undefined
	});

	// History messages state
	const [ historyState, setHistoryState ] = useState([]);

	// Helper to parse any inbound WebSocket event
	const parseInboundEvent = (message) => {
		if(!message || !message.body) {
			throw Error('Empty message');
		}

		const parsedMessage = JSON.parse(message.body);
		if(!parsedMessage || !parsedMessage.payload) {
			throw Error(`Invalid message: ${JSON.stringify(parsedMessage)}`);
		}

		const event = parsedMessage.payload;
		if(!event.sessionId) {
			throw Error(`Message has no sessionId: ${JSON.stringify(event)}`);
		}
		if(domainState.sessionId !== event.sessionId) {
			throw Error('Message is from another session');
		}

		return parsedMessage.payload;
	};

	// Helper to parse any inbound WebSocket action event
	const parseInboundActionEvent = (message) => {
		const event = parseInboundEvent(message);
		if(!event.executionId) {
			throw Error(`Message has no executionId: ${JSON.stringify(event)}`);
		}
		if(actionExecutionState.executionId !== event.executionId) {
			throw Error('Message is from another execution');
		}
		return event;
	};

	// WebSocket subscription for domain events
	useSubscription(WebSocketConstants.inboundDomainChannel, (message) => {
		try {
			const event = parseInboundEvent(message);
			if(!event.actions || !event.actions.length) {
				throw Error(`Domain event has no actions: ${JSON.stringify(event)}`);
			}

			setDomainState((oldState) => {
				return {
					...oldState,
					domainInitialized: true,
					actions: event.actions
				};
			});
		}
		catch(e) {
			console.error(e);
		}
	});

	// WebSocket subscription for history events
	useSubscription(WebSocketConstants.inboundHistoryChannel, (message) => {
		try {
			const event = parseInboundEvent(message);
			if(!event.message || !event.type || !event.timestamp) {
				throw Error(`History event has no message or type: ${JSON.stringify(event)}`);
			}

			setHistoryState((oldState) => {
				return [
					...oldState,
					{
						type: event.type,
						text: Utils.formatHtmlText(event.message),
						formattedDate: Utils.formatDate(new Date(event.timestamp))
					}
				];
			});
		}
		catch(e) {
			console.error(e);
		}
	});

	// WebSocket subscription for action status events
	useSubscription(WebSocketConstants.inboundActionStatusChannel, (message) => {
		try {
			const event = parseInboundActionEvent(message);
			if(!event.status) {
				throw Error(`Action status event has no status: ${JSON.stringify(event)}`);
			}
			
			setActionExecutionState((oldState) => {
				switch(event.status) {
					case 'SUCCESS':
					case 'FAILURE':
						return {
							...oldState,
							executionId: undefined,
							status: 'USER_INPUT'
						};

					case 'SUSPENSION':
						return {
							...oldState,
							status: 'SUSPENDED'
						};

					default:
						throw Error(`Unmapped action status event: ${event.status}`);
				}
			});
		}
		catch(e) {
			console.error(e);
		}
	});

	// The WebSocket client
	const stompClient = useStompClient();

	// Helper to send any outbound WebSocket event
	const sendEvent = useCallback((destination, event) => {
		if(!stompClient) {
			console.error('Not connected to WebSocket');
			return;
		}

		event.sessionId = domainState.sessionId;

		stompClient.publish({
			destination: destination,
			body: JSON.stringify(event)
		});
	}, [ stompClient, domainState.sessionId ]);

	// Helper callback to select an action from the list
	const onSelectAction = (action) => {
		setActionExecutionState((oldState) => {
			return {
				...oldState,
				executionId: undefined,
				currentAction: action,
				status: 'USER_INPUT'
			};
		});
	};

	// Helper callback to exit the current action
	const onExitAction = () => {
		setActionExecutionState((oldState) => {
			return {
				...oldState,
				executionId: undefined,
				currentAction: undefined,
				status: undefined
			};
		});
	};

	// Helper callback to start the current action
	const onStartAction = (variableValues, extraParameters) => {
		const actionName = actionExecutionState.currentAction.name;
		const actionType = actionExecutionState.currentAction.type;
		const executionId = uuidv4();

		setActionExecutionState((oldState) => {
			return {
				...oldState,
				executionId: executionId,
				status: 'EXECUTING_RUN'
			};
		});

		const outboundChannel = WebSocketConstants.outboundRunActionChannelsMap[actionType];
		if(!outboundChannel) {
			console.error(`Unmapped outbound run channel for action type: ${actionType}`);
			return;
		}

		sendEvent(outboundChannel, {
			...extraParameters,
			actionName: actionName,
			executionId: executionId,
			variableValues: variableValues
		});
	};

	// Helper callback to resume the current action after a suspension
	const onResumeAction = (extraParameters) => {
		const actionName = actionExecutionState.currentAction.name;
		const actionType = actionExecutionState.currentAction.type;
		const executionId = actionExecutionState.executionId;

		setActionExecutionState((oldState) => {
			return {
				...oldState,
				status: 'EXECUTING_RESUME'
			};
		});

		const outboundChannel = WebSocketConstants.outboundResumeActionChannelsMap[actionType];
		if(!outboundChannel) {
			console.error(`Unmapped outbound resume channel for action type: ${actionType}`);
			return;
		}

		sendEvent(outboundChannel, {
			...extraParameters,
			actionName: actionName,
			executionId: executionId
		});
	};

	// Helper callback to cancel the current action after a suspension
	const onCancelAction = () => {
		const actionName = actionExecutionState.currentAction.name;
		const executionId = actionExecutionState.executionId;

		setActionExecutionState((oldState) => {
			return {
				...oldState,
				executionId: executionId,
				status: 'USER_INPUT'
			};
		});

		sendEvent(WebSocketConstants.outboundCancelActionChannel, {
			actionName: actionName,
			executionId: executionId
		});
	};

	// Effect to send the init event if the domain is not initialized
	useEffect(() => {
		if(stompClient && !domainState.domainInitialized) {
			sendEvent(WebSocketConstants.outboundInitSessionChannel, {});
		}
	}, [ stompClient, sendEvent, domainState.domainInitialized ]);

	return (
		<div className='main-container'>
			<Overlay
				backEndConnected={Boolean(stompClient)}/>
			<ActionsSection
				domainInitialized={domainState.domainInitialized}
				actions={domainState.actions}
				currentAction={actionExecutionState.currentAction}
				executionStatus={actionExecutionState.status}
				onSelectAction={onSelectAction}
				onExitAction={onExitAction}
				onStartAction={onStartAction}
				onResumeAction={onResumeAction}
				onCancelAction={onCancelAction}
			/>
			<HistorySection
				messages={historyState}
			/>
		</div>
	);
};

export default MainPage;
