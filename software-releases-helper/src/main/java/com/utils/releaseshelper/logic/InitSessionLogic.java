package com.utils.releaseshelper.logic;

import org.springframework.stereotype.Component;

import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.DomainModel;
import com.utils.releaseshelper.model.logic.DomainEvent;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InitSessionEvent;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic component to initialize a user session
 */
@Slf4j
@Component
public class InitSessionLogic extends Logic {

	public InitSessionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter) {
		
		super(globalContext, webSocketOutboundAdapter);
	}

	public void run(InitSessionEvent inboundEvent) {
		
		try {
			
			validateInitSessionEvent(inboundEvent);
			
			DomainModel domain = globalContext.getDomainModel();
			
			// Send the list of action definitions to the client
			DomainEvent outboundEvent = new DomainEvent();
			outboundEvent.setSessionId(inboundEvent.getSessionId());
			outboundEvent.setActions(domain.getLogicData().getActions());
			webSocketOutboundAdapter.sendDomainData(outboundEvent);
			
			// Send the "welcome" message
			sendHistoryEvent(inboundEvent, "Session started, welcome!", HistoryEvent.Type.INFO);
		}
		catch(Exception e) {
			
			log.error("Init session error", e);
			sendHistoryEvent(inboundEvent, "Failed to initialize session: " + e.getMessage(), HistoryEvent.Type.ERROR);
		}
	}
	
	private void validateInitSessionEvent(InitSessionEvent inboundEvent) {
		
		validateInboundEvent(inboundEvent);
	}
}
