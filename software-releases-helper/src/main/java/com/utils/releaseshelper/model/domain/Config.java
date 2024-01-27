package com.utils.releaseshelper.model.domain;

import lombok.Data;

/**
 * The main util configurations
 */
@Data
public class Config {

	private boolean webGui;
	private boolean testMode;
	private boolean printPasswords;
	private JenkinsConfig jenkins;
	private GitConfig git;
}
