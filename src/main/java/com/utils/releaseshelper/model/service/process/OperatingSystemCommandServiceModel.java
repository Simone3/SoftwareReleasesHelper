package com.utils.releaseshelper.model.service.process;

import lombok.Data;

/**
 * A generic operating system command
 */
@Data
public class OperatingSystemCommandServiceModel {

	private String command;
	private boolean suppressOutput;
}
