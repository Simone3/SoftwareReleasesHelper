package com.utils.releaseshelper.model.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * A property for an action
 * It contains all fields of all types of actions because Spring properties do not support polymorphism
 */
@Data
public class ActionProperty {
	
	// Common
	private String name;
	private ActionTypeProperty type;
	private Boolean skipConfirmation;
	
	// Define variables
	private Map<String, String> variables;
	
	// Git
	private String repositoryFolder;
	private List<GitMergeProperty> merges;
	private GitCommitProperty gitCommit;

	// Jenkins
	private String url;
	private Map<String, String> parameters;
	
	// Maven
	private String projectFolder;
	
	// OS commands
	private String folder;
	
	// Maven and OS commands
	private List<GenericCommandProperty> commands;
	
	// Chain
	private List<String> actions;
}
