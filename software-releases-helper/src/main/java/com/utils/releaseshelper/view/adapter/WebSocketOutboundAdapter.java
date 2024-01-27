package com.utils.releaseshelper.view.adapter;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.DomainEvent;
import com.utils.releaseshelper.model.logic.Event;
import com.utils.releaseshelper.model.logic.HistoryEvent;

import lombok.RequiredArgsConstructor;

/**
 * Spring Component to send outbound WebSocket events
 */
@Component
@RequiredArgsConstructor
public class WebSocketOutboundAdapter implements ViewAdapter {

	private final SimpMessagingTemplate simpMessagingTemplate;

	public void sendDomainData(DomainEvent event) {
		
		sendEvent(event, "/topic/domain");
	}
	
	public void sendHistoryEvent(HistoryEvent event) {
		
		sendEvent(event, "/topic/history");
	}
	
	public void sendActionStatusEvent(ActionStatusEvent event) {
		
		sendEvent(event, "/topic/action/status");
	}
	
	private void sendEvent(Event event, String destination) {
		
		simpMessagingTemplate.convertAndSend(destination, new GenericMessage<>(event));
	}
}
