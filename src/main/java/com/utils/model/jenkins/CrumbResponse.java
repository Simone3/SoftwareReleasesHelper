package com.utils.model.jenkins;

import lombok.Data;

@Data
public class CrumbResponse {

	private String _class;
	private String crumb;
	private String crumbRequestField;
}
