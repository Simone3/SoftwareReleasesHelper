package com.utils.releaseshelper.model.service.maven;

import java.util.List;

import lombok.Data;

/**
 * Service input for the Maven command invocations
 */
@Data
public class MavenRunCommandServiceInput {

	private String projectFolder;
	private List<MavenCommandServiceModel> commands;
}
