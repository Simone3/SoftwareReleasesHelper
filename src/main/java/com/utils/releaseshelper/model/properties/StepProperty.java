package com.utils.releaseshelper.model.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * A property for a step
 * It contains all fields of all types of actions because Spring properties do not support polymorphism
 */
@Data
public class StepProperty {
	
	// Common
	private StepTypeProperty type;
	
	// Run actions
	private List<String> actions;
	
	// Pick projects
	private String customPrompt;
	private List<String> projects;
	
	// Run actions for each project
	private Map<String, List<String>> projectActions;
}
