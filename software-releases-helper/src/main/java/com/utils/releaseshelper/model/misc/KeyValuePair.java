package com.utils.releaseshelper.model.misc;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A generic key-value pair
 */
@Data
@AllArgsConstructor
public class KeyValuePair implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	private String value;
}
