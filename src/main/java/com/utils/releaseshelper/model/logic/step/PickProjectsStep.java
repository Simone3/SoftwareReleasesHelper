package com.utils.releaseshelper.model.logic.step;

import java.util.List;

import com.utils.releaseshelper.model.logic.Project;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Step to select one or more projects
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PickProjectsStep extends Step {
	
	private String customPrompt;
	private List<Project> projects;
	
	@Override
	public String getTypeDescription() {
		
		return "Pick Projects";
	}
}
