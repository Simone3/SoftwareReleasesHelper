package com.utils.releaseshelper.model.dto.rest;

import lombok.Data;

/**
 * DTO for Jenkins REST crumb API
 */
@Data
public class CrumbResponse {

	private String _class;
	private String crumb;
	private String crumbRequestField;
}
