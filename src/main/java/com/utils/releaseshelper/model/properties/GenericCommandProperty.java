package com.utils.releaseshelper.model.properties;

import java.util.Map;

import lombok.Data;

/**
 * A property for a generic command
 * It contains all fields of all types of commands because Spring properties do not support polymorphism
 */
@Data
public class GenericCommandProperty {

	private String command;
	private String goals;
	private Map<String, String> arguments;
	private Boolean offline;
	private Boolean suppressOutput;
}
