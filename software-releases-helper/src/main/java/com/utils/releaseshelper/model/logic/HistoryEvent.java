package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Outbound event for history messages
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HistoryEvent extends OutboundEvent {

	private String message;
	private Type type;
	private long timestamp;
	
	public enum Type {
		
		INFO, SUCCESS, ERROR, WARNING;
	}
}
