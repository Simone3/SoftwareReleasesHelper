package com.utils.releaseshelper.model.service.jenkins;

import java.util.Map;

import lombok.Data;

/**
 * Service input for a Jenkins build
 */
@Data
public class JenkinsBuildServiceInput {

	private String url;
	private Map<String, String> parameters;
}
