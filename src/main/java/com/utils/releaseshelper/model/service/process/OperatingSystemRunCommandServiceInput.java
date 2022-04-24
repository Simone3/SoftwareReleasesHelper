package com.utils.releaseshelper.model.service.process;

import java.util.List;

import lombok.Data;

/**
 * Service input for the generic operating system command invocations
 */
@Data
public class OperatingSystemRunCommandServiceInput {

	private String folder;
	private List<OperatingSystemCommandServiceModel> commands;
}
