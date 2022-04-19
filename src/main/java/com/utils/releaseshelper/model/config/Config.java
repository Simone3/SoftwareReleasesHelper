package com.utils.releaseshelper.model.config;

import lombok.Data;

/**
 * The main util configurations
 */
@Data
public class Config {

	private boolean testMode;
	private boolean printPasswords;
	private JenkinsConfig jenkins;
	private GitConfig git;
	private MavenConfig maven;
}
