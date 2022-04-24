package com.utils.releaseshelper.model.service.maven;

import java.util.Map;

import lombok.Data;

/**
 * A Maven command
 */
@Data
public class MavenCommandServiceModel {

	private String goals;
	private Map<String, String> arguments;
	private boolean offline;
	private boolean suppressOutput;
}
