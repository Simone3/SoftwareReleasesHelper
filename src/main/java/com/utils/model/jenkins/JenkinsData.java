package com.utils.model.jenkins;

import lombok.Data;

@Data
public class JenkinsData {
	
	private String baseUrl;
	private String crumbUrl;
	private String username;
	private String password;
	private boolean insecureHttps;
	private int timeoutMilliseconds;
}
