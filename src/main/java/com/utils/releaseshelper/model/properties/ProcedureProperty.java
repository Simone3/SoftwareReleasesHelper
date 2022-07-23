package com.utils.releaseshelper.model.properties;

import java.util.List;

import lombok.Data;

/**
 * A property for a procedure
 */
@Data
public class ProcedureProperty {
	
	private String name;
	private List<StepProperty> steps;
}
