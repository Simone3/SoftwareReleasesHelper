package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * A property for a generic command
 */
@Data
public class OperatingSystemCommandProperty {

	private String command;
	private Boolean suppressOutput;
}
