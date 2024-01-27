package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Outbound event for action statuses
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActionStatusEvent extends OutboundActionEvent {

	private Type status;
	
	public enum Type {
		
		SUCCESS, FAILURE, SUSPENSION;
	}
}
