package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.domain.Action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Outbound event for the app domain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DomainEvent extends OutboundEvent {

	private List<Action> actions;
}
