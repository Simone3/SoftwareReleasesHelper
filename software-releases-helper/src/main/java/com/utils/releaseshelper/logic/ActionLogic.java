package com.utils.releaseshelper.logic;

import java.util.List;

import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.Action;
import com.utils.releaseshelper.model.domain.VariableDefinition;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.ActionState;
import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.InboundActionEvent;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.utils.ValidationUtils;
import com.utils.releaseshelper.utils.VariableUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

/**
 * A Logic component for actions with some helper methods
 */
abstract class ActionLogic extends Logic {
	
	protected ActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter) {
		
		super(globalContext, webSocketOutboundAdapter);
	}

	protected void validateInboundActionEvent(InboundActionEvent inboundEvent) {
		
		validateInboundEvent(inboundEvent);
		ValidationUtils.notBlank(inboundEvent.getExecutionId(), "Execution ID is blank");
		ValidationUtils.notBlank(inboundEvent.getActionName(), "Action name is blank");
	}

	@SuppressWarnings("unchecked")
	protected <T> T getAction(String actionName, Class<T> actionType) {
		
		List<Action> actions = globalContext.getDomainModel().getLogicData().getActions();
		for(Action action: actions) {
			
			if(action.getName().equals(actionName) && action.getClass().equals(actionType)) {
				
				return (T) action;
			}
		}
		
		throw new BusinessException("No action found with name \"" + actionName + "\" and type \"" + actionType + "\"");
	}
	
	protected List<KeyValuePair> getVariableValues(Action action, List<KeyValuePair> userValues) {
		
		List<VariableDefinition> sourceVariables = action.getVariables();
		return VariableUtils.getVariableValues(sourceVariables, userValues);
	}
	
	protected void sendActionStatusEvent(InboundActionEvent sourceInboundEvent, ActionStatusEvent.Type status) {
		
		ActionStatusEvent outboundEvent = new ActionStatusEvent();
		outboundEvent.setSessionId(sourceInboundEvent.getSessionId());
		outboundEvent.setExecutionId(sourceInboundEvent.getExecutionId());
		outboundEvent.setActionName(sourceInboundEvent.getActionName());
		outboundEvent.setStatus(status);
		
		webSocketOutboundAdapter.sendActionStatusEvent(outboundEvent);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends ActionState> T popSuspensionState(InboundActionEvent inboundEvent, Class<T> stateClass) {
		
		String stateKey = getSuspensionStateKey(inboundEvent);
		ActionState state = globalContext.getGlobalState().removeSuspensionState(stateKey);
		if(state == null || !state.getClass().equals(stateClass)) {
			
			throw new BusinessException("Cannot find suspension state for this action");
		}
		return (T) state;
	}
	
	protected void removeSuspensionState(InboundActionEvent inboundEvent) {
		
		String stateKey = getSuspensionStateKey(inboundEvent);
		globalContext.getGlobalState().removeSuspensionState(stateKey);
	}
	
	protected void saveSuspensionState(InboundActionEvent inboundEvent, ActionState state) {
		
		String key = getSuspensionStateKey(inboundEvent);
		globalContext.getGlobalState().putSuspensionState(key, state);
	}
	
	private String getSuspensionStateKey(InboundActionEvent inboundEvent) {
		
		return inboundEvent.getSessionId() + "-" + inboundEvent.getExecutionId();
	}
}
