package com.utils.model.git;

import lombok.Data;

@Data
public class GitData {

	private String basePath;
	private String username;
	private String password;
	private String mergeMessage;
	private int timeoutMilliseconds;
}
