package com.utils.model.jenkins;

import lombok.Data;

@Data
public class JenkinsParameter {

	private String key;
	private String value;
	private boolean askMe;
	private boolean removeWhitespace;
}
