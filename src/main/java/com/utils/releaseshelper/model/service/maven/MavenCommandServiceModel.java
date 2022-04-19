package com.utils.releaseshelper.model.service.maven;

import java.util.Map;

import lombok.Data;

/**
 * Description of a Maven command
 */
@Data
public class MavenCommandServiceModel {

	private String goals;
	private Map<String, String> arguments;
	private boolean printMavenOutput;
}
