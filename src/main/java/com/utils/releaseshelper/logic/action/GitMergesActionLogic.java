package com.utils.releaseshelper.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.model.logic.action.GitMergesAction;
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

	public GitMergesActionLogic(GitMergesAction action, Map<String, String> variables, CommandLineInterface cli, GitService gitService) {
		
		super(action, variables, cli);
		this.gitService = gitService;
	}
	
	@Override
	protected void beforeAction() {
		
		// Do nothing here for now
	}

	@Override
	protected void registerValueDefinitions(ValuesDefiner valuesDefiner) {
		
		valuesDefiner.addValueDefinition(action.getRepositoryFolder(), "repository folder");
		valuesDefiner.addValueDefinition(action.getMerges(), "merge definitions");
	}

	@Override
	protected void printActionDescription(ValuesDefiner valuesDefiner) {
		
		String repositoryFolder = valuesDefiner.getValue(action.getRepositoryFolder());
		boolean pull = action.isPull();
		String merges = valuesDefiner.getValue(action.getMerges());
		
		cli.println("Git merges on %s:", repositoryFolder);
		cli.println("%s", merges);
		cli.println("(%s)", pull ? "pulling from all branches" : "without pulling");
		
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

		String repositoryFolder = valuesDefiner.getValue(action.getRepositoryFolder());
		String merges = valuesDefiner.getValue(action.getMerges());
		
		GitMergesServiceInput mergeInput = new GitMergesServiceInput();
		mergeInput.setRepositoryFolder(repositoryFolder);
		mergeInput.setMerges(mapMergeServiceModels(action.isPull(), merges));
		return mergeInput;
	}

	private List<GitMergeServiceModel> mapMergeServiceModels(boolean pull, String mergesString) {
		
		List<GitMergeServiceModel> serviceModels = new ArrayList<>();
		
		String[] mergesStrings = mergesString.split(";");
		
		for(String mergeString: mergesStrings) {
			
			String[] mergeBranches = mergeString.split("->");
			
			if(mergeBranches.length < 2) {
				
				throw new IllegalStateException("Not enough branches defined in: " + mergeString);
			}
			
			for(int i = 1; i < mergeBranches.length; i++) {
				
				String sourceBranch = mergeBranches[i - 1];
				String targetBranch = mergeBranches[i];

				if(StringUtils.isBlank(sourceBranch) || StringUtils.isBlank(targetBranch)) {
					
					throw new IllegalStateException("Empty branch in: " + mergeString);
				}
				
				GitMergeServiceModel serviceModel = new GitMergeServiceModel();
				serviceModel.setPull(pull);
				serviceModel.setSourceBranch(sourceBranch.trim());
				serviceModel.setTargetBranch(targetBranch.trim());
				serviceModels.add(serviceModel);
			}
		}

		return serviceModels;
	}
}
