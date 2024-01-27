package com.utils.releaseshelper.model.domain;

import lombok.Data;

/**
 * The Jenkins configuration
 */
@Data
public class JenkinsConfig {

	private String baseUrl;
	private String crumbUrl;
	private String username;
	private String password;
	private boolean useCrumb;
	private boolean insecureHttps;
	private int timeoutMilliseconds;
}
