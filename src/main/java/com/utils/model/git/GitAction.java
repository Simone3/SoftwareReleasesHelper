package com.utils.model.git;

import java.util.List;

import com.utils.model.main.Action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GitAction extends Action {
	
	private String repositoryFolder;
	private List<GitOperation> operations;
}
