package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.action.GitMergesAction;
import com.utils.releaseshelper.model.logic.git.GitMerge;
import com.utils.releaseshelper.model.service.git.GitMergeServiceModel;
import com.utils.releaseshelper.model.service.git.GitMergesServiceInput;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.utils.ValuesDefiner;
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
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {
		
		List<GitMerge> merges = action.getMerges();
		for(GitMerge merge: merges) {
			
			valuesDefiner.addValueDefinition(merge.getSourceBranch(), "source branch");
			valuesDefiner.addValueDefinition(merge.getTargetBranch(), "target branch");
		}
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		String repositoryFolder = action.getRepositoryFolder();
		
		cli.println("Git merges on %s:", repositoryFolder);
		
		List<GitMerge> merges = action.getMerges();
		for(GitMerge merge: merges) {
			
			boolean pull = merge.isPull();
			String sourceBranch = valuesDefiner.getValue(merge.getSourceBranch());
			String targetBranch = valuesDefiner.getValue(merge.getTargetBranch());
			cli.println("  - From %s to %s (%s)", sourceBranch, targetBranch, pull ? "pulling from both branches" : "without pulling");
		}
		
		cli.println();
	}
	
	@Override
	protected String getConfirmationPrompt() {
		
		return "Start merges";
	}

	@Override
	protected void doRunAction(ValuesDefiner valuesDefiner) {
		
		GitMergesServiceInput mergesInput = mapMergesServiceInput(valuesDefiner);
		gitService.merges(mergesInput);
	}

	@Override
	protected void afterAction() {
		
		// Do nothing here for now
	}
	
	private GitMergesServiceInput mapMergesServiceInput(ValuesDefiner valuesDefiner) {
		
		GitMergesServiceInput mergeInput = new GitMergesServiceInput();
		mergeInput.setRepositoryFolder(action.getRepositoryFolder());
		mergeInput.setMerges(mapMergeServiceModels(valuesDefiner, action.getMerges()));
		return mergeInput;
	}

	private List<GitMergeServiceModel> mapMergeServiceModels(ValuesDefiner valuesDefiner, List<GitMerge> merges) {
		
		List<GitMergeServiceModel> serviceModels = new ArrayList<>();
		
		for(int i = 0; i < merges.size(); i++) {
			
			GitMerge merge = merges.get(i);
			String sourceBranch = valuesDefiner.getValue(merge.getSourceBranch());
			String targetBranch = valuesDefiner.getValue(merge.getTargetBranch());
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
