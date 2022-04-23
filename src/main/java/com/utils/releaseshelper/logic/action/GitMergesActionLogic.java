package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.git.GitMerge;
import com.utils.releaseshelper.model.service.git.GitMergeServiceModel;
import com.utils.releaseshelper.model.service.git.GitMergesServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.utils.VariablesUtils;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more Git merge operations
 */
public class GitMergesActionLogic extends ActionLogic<GitMergesAction> {

	private final GitService gitService;
	
	private List<String> sourceBranches = new ArrayList<>();
	private List<String> targetBranches = new ArrayList<>();

	protected GitMergesActionLogic(GitMergesAction action, Map<String, String> variables, CommandLineInterface cli, GitService gitService) {
		
		super(action, variables, cli);
		this.gitService = gitService;
	}
	
	@Override
	protected void beforeAction() {
		
		defineBranches();
	}

	@Override
	protected void printActionDescription() {
		
		String repositoryFolder = action.getRepositoryFolder();
		
		cli.println("Git merges on %s:", repositoryFolder);
		
		List<GitMerge> merges = action.getMerges();
		for(int i = 0; i < merges.size(); i++) {
			
			GitMerge merge = merges.get(i);
			boolean pull = merge.isPull();
			String sourceBranch = sourceBranches.get(i);
			String targetBranch = targetBranches.get(i);
			cli.println("  - From %s to %s (%s)", sourceBranch, targetBranch, pull ? "pulling from both branches" : "without pulling");
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Start merges";
	}

	@Override
	protected void doRunAction() {
		
		GitMergesServiceInput mergesInput = mapMergesServiceInput();
		gitService.merges(mergesInput);
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}

	private void defineBranches() {
		
		List<GitMerge> merges = action.getMerges();
		for(int i = 0; i < merges.size(); i++) {
			
			GitMerge merge = merges.get(i);
			sourceBranches.add(VariablesUtils.defineValue(cli, "Define source branch for merge at index " + i, merge.getSourceBranch(), variables));
			targetBranches.add(VariablesUtils.defineValue(cli, "Define target branch for merge at index " + i, merge.getTargetBranch(), variables));
		}
	}
	
	private GitMergesServiceInput mapMergesServiceInput() {
		
		GitMergesServiceInput mergeInput = new GitMergesServiceInput();
		mergeInput.setRepositoryFolder(action.getRepositoryFolder());
		mergeInput.setMerges(mapMergeServiceModels(action.getMerges()));
		return mergeInput;
	}

	private List<GitMergeServiceModel> mapMergeServiceModels(List<GitMerge> merges) {
		
		List<GitMergeServiceModel> serviceModels = new ArrayList<>();
		
		for(int i = 0; i < merges.size(); i++) {
			
			GitMerge merge = merges.get(i);
			String sourceBranch = sourceBranches.get(i);
			String targetBranch = targetBranches.get(i);
			serviceModels.add(mapMergeServiceModels(merge, sourceBranch, targetBranch));
		}
		
		
		return serviceModels;
	}

	private GitMergeServiceModel mapMergeServiceModels(GitMerge merge, String sourceBranch, String targetBranch) {
		
		GitMergeServiceModel model = new GitMergeServiceModel();
		model.setPull(merge.isPull());
		model.setSourceBranch(sourceBranch);
		model.setTargetBranch(targetBranch);
		return model;
	}
}
