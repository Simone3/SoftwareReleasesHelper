package com.utils.releaseshelper.logic;

import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InboundEvent;
import com.utils.releaseshelper.utils.ValidationUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * A Logic component is the middle layer of the application
 * Its purpose is to run the main business logic
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class Logic {

	protected final GlobalContext globalContext;
	protected final WebSocketOutboundAdapter webSocketOutboundAdapter;
	
	protected void validateInboundEvent(InboundEvent inboundEvent) {
		
		ValidationUtils.notBlank(inboundEvent.getSessionId(), "Session ID is blank");
	}
	
	protected void sendHistoryEvent(InboundEvent sourceInboundEvent, String message, HistoryEvent.Type type) {
		
		HistoryEvent outboundEvent = new HistoryEvent();
		outboundEvent.setSessionId(sourceInboundEvent.getSessionId());
		outboundEvent.setTimestamp(System.currentTimeMillis());
		outboundEvent.setType(type);
		outboundEvent.setMessage(message);
		
		webSocketOutboundAdapter.sendHistoryEvent(outboundEvent);
	}
}
