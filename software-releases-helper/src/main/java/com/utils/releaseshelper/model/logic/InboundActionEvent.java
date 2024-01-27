package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.misc.KeyValuePair;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A generic inbound event for actions
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class InboundActionEvent extends InboundEvent {

	private String executionId;
	private String actionName;
	private List<KeyValuePair> variableValues;
}
