package com.utils.releaseshelper.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Inbound event to resume a Git Merges Action
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GitMergesResumeActionEvent extends ResumeActionEvent {
	
}
