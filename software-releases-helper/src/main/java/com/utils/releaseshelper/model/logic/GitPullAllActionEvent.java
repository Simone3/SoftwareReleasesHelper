package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to start Git Pull All Action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitPullAllActionEvent extends InboundActionEvent {

}
