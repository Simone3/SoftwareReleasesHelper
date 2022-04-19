package com.utils.releaseshelper.model.service.git;

import java.util.List;

import lombok.Data;

/**
 * Service input for the Git merge operations
 */
@Data
public class GitMergesServiceInput {

	private String repositoryFolder;
	private List<GitMergeServiceModel> merges;
}
