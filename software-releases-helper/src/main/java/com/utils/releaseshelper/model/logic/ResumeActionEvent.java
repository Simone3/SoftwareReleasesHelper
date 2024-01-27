package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A generic inbound event to resume an action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ResumeActionEvent extends InboundActionEvent {
	
}
