package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.misc.KeyValuePair;

import lombok.Data;

/**
 * The state for a Jenkins Build Action
 */
@Data
public class JenkinsBuildActionState implements ActionState {
	
	private static final long serialVersionUID = 1L;
	
	private String url;
	private List<KeyValuePair> parameters;
}
