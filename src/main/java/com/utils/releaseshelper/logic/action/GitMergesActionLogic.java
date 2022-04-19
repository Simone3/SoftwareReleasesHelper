package com.utils.releaseshelper.logic.action;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.git.GitMerge;
import com.utils.releaseshelper.model.service.git.GitMergeServiceModel;
import com.utils.releaseshelper.model.service.git.GitMergesServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.view.CommandLineInterface;

/**
 * Logic to execute the action to run one or more Git merge operations
 */
public class GitMergesActionLogic extends ActionLogic<GitMergesAction> {

	private final GitService gitService;

	protected GitMergesActionLogic(GitMergesAction action, Map<String, String> variables, CommandLineInterface cli, GitService gitService) {
		
		super(action, variables, cli);
		this.gitService = gitService;
	}
	
	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void printActionDescription() {
		
		String repositoryFolder = action.getRepositoryFolder();
		
		cli.println("Git merges on %s:", repositoryFolder);
		
		for(GitMerge merge: action.getMerges()) {

			boolean pull = merge.isPull();
			String sourceBranch = merge.getSourceBranch();
			String targetBranch = merge.getTargetBranch();
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
	
	private GitMergesServiceInput mapMergesServiceInput() {
		
		GitMergesServiceInput mergeInput = new GitMergesServiceInput();
		mergeInput.setRepositoryFolder(action.getRepositoryFolder());
		mergeInput.setMerges(mapMergeServiceModels(action.getMerges()));
		return mergeInput;
	}

	private List<GitMergeServiceModel> mapMergeServiceModels(List<GitMerge> merges) {
		
		return merges.stream().map(this::mapMergeServiceModels).collect(Collectors.toList());
	}

	private GitMergeServiceModel mapMergeServiceModels(GitMerge merge) {
		
		GitMergeServiceModel model = new GitMergeServiceModel();
		model.setPull(merge.isPull());
		model.setSourceBranch(merge.getSourceBranch());
		model.setTargetBranch(merge.getTargetBranch());
		return model;
	}
}
