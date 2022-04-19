package com.utils.releaseshelper.model.config;

import lombok.Data;

/**
 * The Jenkins configuration
 */
@Data
public class JenkinsConfig {

	private String crumbUrl;
	private String username;
	private String password;
	private boolean insecureHttps;
	private int timeoutMilliseconds;
}
