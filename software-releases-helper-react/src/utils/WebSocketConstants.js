
/**
 * Constants for WebSockets
 */
class WebSocketConstants {

	/* ***** URLs *****/

	static webSocketUrl = 'ws://localhost:10333/websocket';

	/* ***** Outbound channels *****/

	static outboundInitSessionChannel = '/app/session/init/run';

	static outboundCancelActionChannel = '/app/action/cancel';

	static outboundRunActionChannelsMap = {
		JENKINS_BUILD: '/app/action/jenkins/build/run',
		GIT_MERGES: '/app/action/git/merge/run',
		OPERATING_SYSTEM_COMMANDS: '/app/action/os/commands/run'
	};

	static outboundResumeActionChannelsMap = {
		GIT_MERGES: '/app/action/git/merge/resume'
	};

	/* ***** Inbound channels *****/

	static inboundDomainChannel = '/topic/domain';

	static inboundHistoryChannel = '/topic/history';

	static inboundActionStatusChannel = '/topic/action/status';
}

export default WebSocketConstants;
