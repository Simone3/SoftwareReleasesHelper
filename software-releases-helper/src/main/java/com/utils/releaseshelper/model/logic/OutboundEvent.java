package com.utils.releaseshelper.model.logic;

import lombok.Data;

/**
 * A generic outbound event 
 */
@Data
public abstract class OutboundEvent implements Event {

	private String sessionId;
}
