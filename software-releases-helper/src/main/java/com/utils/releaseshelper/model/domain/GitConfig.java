package com.utils.releaseshelper.model.domain;

import lombok.Data;

/**
 * The Git configurations
 */
@Data
public class GitConfig {

	private String basePath;
	private String username;
	private String password;
	private String mergeMessage;
	private int timeoutMilliseconds;
}
