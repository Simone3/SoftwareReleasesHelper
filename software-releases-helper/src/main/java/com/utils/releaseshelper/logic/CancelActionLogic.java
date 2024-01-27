package com.utils.releaseshelper.logic;

import org.springframework.stereotype.Component;

import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.logic.CancelActionEvent;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

/**
 * Logic component to cancel a suspended action
 */
@Component
public class CancelActionLogic extends ActionLogic {
	
	public CancelActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter) {
		
		super(globalContext, webSocketOutboundAdapter);
	}

	public void run(CancelActionEvent inboundEvent) {
		
		validateRunEvent(inboundEvent);
		
		// Simply remove the saved state
		removeSuspensionState(inboundEvent);
	}
	
	private void validateRunEvent(CancelActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}
}
