package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A generic outbound event for actions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OutboundActionEvent extends OutboundEvent {

	private String executionId;
	private String actionName;
}
