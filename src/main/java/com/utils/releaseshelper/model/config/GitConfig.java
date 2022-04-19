package com.utils.releaseshelper.model.config;

import lombok.Data;

/**
 * The Git configurations
 */
@Data
public class GitConfig {

	private String username;
	private String password;
	private String mergeMessage;
	private int timeoutMilliseconds;
}
