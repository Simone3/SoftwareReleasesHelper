package com.utils.releaseshelper.model.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A generic operating system command 
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class OperatingSystemCommand implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String command;
	private boolean suppressOutput;
}
