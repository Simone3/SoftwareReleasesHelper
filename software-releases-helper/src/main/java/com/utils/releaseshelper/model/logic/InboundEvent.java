package com.utils.releaseshelper.model.logic;

import lombok.Data;

/**
 * A generic inbound event
 */
@Data
public abstract class InboundEvent implements Event {

	private String sessionId;
}
