package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for a project
 */
@Data
public class ProjectProperty {

	private String name;
	private List<String> actionNames;
}
