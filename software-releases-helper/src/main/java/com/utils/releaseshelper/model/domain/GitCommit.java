package com.utils.releaseshelper.model.domain;

import java.io.Serializable;

import lombok.Data;

/**
 * A Git commit
 */
@Data
public class GitCommit implements Serializable {

	private static final long serialVersionUID = 1L;

	private String branch;
	private boolean pull;
	private String message;
}
