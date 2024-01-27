package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to start a Jenkins Build Action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JenkinsBuildActionEvent extends InboundActionEvent {

}
