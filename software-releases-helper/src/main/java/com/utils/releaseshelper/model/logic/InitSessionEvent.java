package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to initialize a session
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InitSessionEvent extends InboundEvent {

}
