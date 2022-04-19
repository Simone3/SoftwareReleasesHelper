package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * All Jenkins properties
 */
@Data
public class JenkinsProperties {
	
	private String baseUrl;
	private String crumbUrl;
	private String username;
	private String password;
	private Boolean insecureHttps;
	private Integer timeoutMilliseconds;
}
