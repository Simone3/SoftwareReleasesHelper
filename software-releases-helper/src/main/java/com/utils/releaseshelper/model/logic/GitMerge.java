package com.utils.releaseshelper.model.logic;

import java.io.Serializable;

import lombok.Data;

/**
 * A Git merge
 */
@Data
public class GitMerge implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String sourceBranch;
	private String targetBranch;
	private boolean pull;
}
