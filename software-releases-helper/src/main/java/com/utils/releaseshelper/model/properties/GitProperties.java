package com.utils.releaseshelper.model.properties;

import lombok.Data;

/**
 * All Git properties
 */
@Data
public class GitProperties {

	private String basePath;
	private String username;
	private String password;
	private String mergeMessage;
	private Integer timeoutMilliseconds;
}
