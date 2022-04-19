package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for a category
 */
@Data
public class CategoryProperty {

	private String name;
	private List<ProjectProperty> projects;
}
