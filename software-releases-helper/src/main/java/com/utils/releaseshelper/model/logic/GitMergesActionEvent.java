package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to start Git Merges Action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitMergesActionEvent extends InboundActionEvent {

}
