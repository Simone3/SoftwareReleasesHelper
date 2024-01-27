package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to cancel a suspended action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CancelActionEvent extends InboundActionEvent {

}
