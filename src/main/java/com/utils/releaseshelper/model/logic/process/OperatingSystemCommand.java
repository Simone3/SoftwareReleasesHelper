package com.utils.releaseshelper.model.logic.process;

import com.utils.releaseshelper.model.logic.ValueDefinition;

import lombok.Data;

/**
 * A generic operating system command 
 */
@Data
public class OperatingSystemCommand {

	private ValueDefinition command;
	private boolean suppressOutput;
}
