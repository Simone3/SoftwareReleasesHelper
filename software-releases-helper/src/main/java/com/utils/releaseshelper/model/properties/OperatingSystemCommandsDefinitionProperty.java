package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for Operating System Commands Action data
 */
@Data
public class OperatingSystemCommandsDefinitionProperty {
	
	private String folder;
	private List<OperatingSystemCommandProperty> commands;
	private GitCommitProperty gitCommit;
}