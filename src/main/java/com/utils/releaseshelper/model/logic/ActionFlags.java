package com.utils.releaseshelper.model.logic;

import lombok.Data;

/**
 * Container for helper flags for actions
 */
@Data
public class ActionFlags {

	private boolean gitActions;
	private boolean jenkinsActions;
	private boolean mavenActions;
	private boolean operatingSystemActions;
}
