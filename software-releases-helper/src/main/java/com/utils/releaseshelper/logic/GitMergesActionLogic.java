package com.utils.releaseshelper.logic;

import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST_ELEMENT;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST_ELEMENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitRepository;
import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.GitMergesAction;
import com.utils.releaseshelper.model.error.ActionSuspensionException;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.ActionState;
import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.GitMerge;
import com.utils.releaseshelper.model.logic.GitMergesActionEvent;
import com.utils.releaseshelper.model.logic.GitMergesActionState;
import com.utils.releaseshelper.model.logic.GitMergesActionSuspensionStep;
import com.utils.releaseshelper.model.logic.GitMergesResumeActionEvent;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InboundActionEvent;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.utils.VariableUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic component to run the Git Merges Action
 */
@Slf4j
@Component
public class GitMergesActionLogic extends ActionLogic {
	
	private final GitConnector connector;

	public GitMergesActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter, GitConnector connector) {
		
		super(globalContext, webSocketOutboundAdapter);
		this.connector = connector;
	}

	public void run(GitMergesActionEvent inboundEvent) {
		
		try {
			
			validateRunEvent(inboundEvent);
			
			GitMergesActionState state = initializeState(inboundEvent);
			String folder = state.getFolder();
			
			try(GitRepository git = connector.getRepository(folder)) {
				
				doBeforeMerges(git, state);
				doMerges(inboundEvent, git, state);
				doAfterMerges(inboundEvent, git, state);
			}
		}
		catch(ActionSuspensionException e) {
			
			handleSuspension(inboundEvent, e.getHistoryMessage(), e.getCurrentState());
		}
		catch(Exception e) {
			
			handleGenericError(inboundEvent, e);
		}
	}

	public void resume(GitMergesResumeActionEvent inboundEvent) {
		
		try {
			
			validateResumeEvent(inboundEvent);
			
			// Retrieve the previously saved state
			GitMergesActionState state = popSuspensionState(inboundEvent, GitMergesActionState.class);
			GitMergesActionSuspensionStep step = state.getSuspensionStep();
			String folder = state.getFolder();

			try(GitRepository git = connector.getRepository(folder)) {
				
				switch(step) {
				
					// Resume process from the beginning of the "state.getCurrentMergeIndex()"-th merge
					case CURRENT_MERGE_START:
						doMerges(inboundEvent, git, state);
						doAfterMerges(inboundEvent, git, state);
						return;
					
					// Resume process from the "end" of the "state.getCurrentMergeIndex()"-th merge after a manual merge prompt
					case CURRENT_MERGE_MANUAL:
						verifyManualMerge(git, state);
						state.incrementCurrentMergeIndex();
						doMerges(inboundEvent, git, state);
						doAfterMerges(inboundEvent, git, state);
						return;
						
					default:
						throw new IllegalStateException("Unmapped suspension step: " + step);
				}
			}
		}
		catch(ActionSuspensionException e) {
			
			handleSuspension(inboundEvent, e.getHistoryMessage(), e.getCurrentState());
		}
		catch(Exception e) {
			
			handleGenericError(inboundEvent, e);
		}
	}
	
	private void validateRunEvent(GitMergesActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}

	private void validateResumeEvent(GitMergesResumeActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}
	
	private void handleSuspension(InboundActionEvent inboundEvent, String historyMessage, ActionState currentState) {
		
		saveSuspensionState(inboundEvent, currentState);
		sendHistoryEvent(inboundEvent, historyMessage, HistoryEvent.Type.WARNING);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUSPENSION);
	}
	
	private void handleGenericError(InboundActionEvent inboundEvent, Exception e) {
		
		log.error("Git merges error", e);
		sendHistoryEvent(inboundEvent, "Failed to Git merge: " + e.getMessage(), HistoryEvent.Type.ERROR);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.FAILURE);
	}

	private GitMergesActionState initializeState(InboundActionEvent inboundEvent) {
		
		// Get action definition and user inputs
		String actionName = inboundEvent.getActionName();
		GitMergesAction action = getAction(actionName, GitMergesAction.class);
		List<KeyValuePair> userVariableValues = inboundEvent.getVariableValues();
		String folder = action.getRepositoryFolder();
		String mergesString = action.getMerges();
		boolean pull = action.isPull();
		
		// Parse variables for placeholders
		List<KeyValuePair> placeholderValues = getVariableValues(action, userVariableValues);
		
		// Replace placeholders, if any
		folder = VariableUtils.replacePlaceholders(folder, placeholderValues);
		mergesString = VariableUtils.replacePlaceholders(mergesString, placeholderValues);
		
		// Parse merges string
		List<GitMerge> merges = parseMerges(pull, mergesString);
		
		// Build and return the action state
		GitMergesActionState state = new GitMergesActionState();
		state.setFolder(folder);
		state.setMergesString(mergesString);
		state.setMerges(merges);
		state.setPull(pull);
		state.setCurrentMergeIndex(0);
		state.setPulledBranches(new HashSet<>());
		return state;
	}
	
	private void doBeforeMerges(GitRepository git, GitMergesActionState state) {
		
		// Check that all merge branches actually exist
		Set<String> branches = new HashSet<>();
		for(GitMerge merge: state.getMerges()) {
			
			branches.add(merge.getSourceBranch());
			branches.add(merge.getTargetBranch());
		}
		Set<String> nonExistingBranches = connector.checkNonExistingBranches(git, branches);
		if(!nonExistingBranches.isEmpty()) {
			
			throw new BusinessException("These branches do not exist in your local Git repository: " + String.join(", ", nonExistingBranches));
		}
		
		// Save current branch for later use
		String originalBranch = connector.getCurrentBranch(git);
		state.setOriginalBranch(originalBranch);
	}

	private void doMerges(InboundActionEvent inboundEvent, GitRepository git, GitMergesActionState state) {
		
		List<GitMerge> merges = state.getMerges();
		
		while(state.getCurrentMergeIndex() < merges.size()) {

			checkWorkingTreeClean(inboundEvent, git, state);
			doCurrentMerge(inboundEvent, git, state);
			state.incrementCurrentMergeIndex();
		}
	}

	private void doCurrentMerge(InboundActionEvent inboundEvent, GitRepository git, GitMergesActionState state) {

		GitConfig gitConfig = globalContext.getDomainModel().getConfig().getGit();
		Set<String> pulledBranches = state.getPulledBranches();
		GitMerge merge = state.getMerges().get(state.getCurrentMergeIndex());
		boolean pull = merge.isPull();
		String sourceBranch = merge.getSourceBranch();
		String targetBranch = merge.getTargetBranch();
		
		String mergeMessage = getMergeMessage(gitConfig, sourceBranch, targetBranch);
		
		// If needed, pull source branch
		if(pull && !pulledBranches.contains(sourceBranch)) {
			
			switchBranch(inboundEvent, git, sourceBranch, "source branch");
			pullCurrentBranch(inboundEvent, git, gitConfig, state, sourceBranch);
			pulledBranches.add(sourceBranch);
		}
		
		// Switch to target branch and, if needed, pull it
		switchBranch(inboundEvent, git, targetBranch, "target branch");
		if(pull && !pulledBranches.contains(targetBranch)) {

			pullCurrentBranch(inboundEvent, git, gitConfig, state, targetBranch);
			pulledBranches.add(targetBranch);
		}
		
		// Do the actual Git merge
		MergeResult mergeResult = connector.mergeIntoCurrentBranch(git, sourceBranch, mergeMessage);
		MergeStatus mergeStatus = mergeResult.getMergeStatus();
		if(mergeStatus.isSuccessful()) {
			
			sendHistoryEvent(inboundEvent, "Successfully merged " + sourceBranch + " into " + targetBranch + " with status: " + mergeStatus, HistoryEvent.Type.INFO);
		}
		else {
	
			sendHistoryEvent(inboundEvent, "Error merging " + sourceBranch + " into " + targetBranch + ", status is " + mergeStatus, HistoryEvent.Type.ERROR);
			verifyManualMerge(git, state);
		}
	}
	
	private void doAfterMerges(InboundActionEvent inboundEvent, GitRepository git, GitMergesActionState state) {
		
		// Switch back to original branch
		String originalBranch = state.getOriginalBranch();
		switchBranch(inboundEvent, git, originalBranch, "original branch");
		
		// Send the final OK message
		sendHistoryEvent(inboundEvent, "All merges completed! Don't forget to manually push the target branches." + getActionDescription(state), HistoryEvent.Type.SUCCESS);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUCCESS);
	}
	
	private void switchBranch(InboundActionEvent inboundEvent, GitRepository git, String branch, String branchLabel) {
		
		connector.switchBranch(git, branch);
		sendHistoryEvent(inboundEvent, "Switched to " + branch + " (" + branchLabel + ")", HistoryEvent.Type.INFO);
	}
	
	private void checkWorkingTreeClean(InboundActionEvent inboundEvent, GitRepository git, GitMergesActionState state) {
		
		if(!connector.isWorkingTreeClean(git)) {
		
			state.setSuspensionStep(GitMergesActionSuspensionStep.CURRENT_MERGE_START);
			throw new ActionSuspensionException("Working tree is not clean, please commit or discard changed files and then click Resume", state);
		}
		
		sendHistoryEvent(inboundEvent, "Working tree is clean", HistoryEvent.Type.INFO);
	}
	
	private String getMergeMessage(GitConfig gitConfig, String sourceBranch, String targetBranch) {
		
		List<KeyValuePair> mergeMessagePlaceholders = List.of(
			new KeyValuePair("SOURCE_BRANCH", sourceBranch),
			new KeyValuePair("TARGET_BRANCH", targetBranch)
		);
		
		return VariableUtils.replacePlaceholders(gitConfig.getMergeMessage(), mergeMessagePlaceholders);
	}

	private void pullCurrentBranch(InboundActionEvent inboundEvent, GitRepository git, GitConfig gitConfig, GitMergesActionState state, String branchName) {
		
		try {
			
			String username = gitConfig.getUsername();
			String password = gitConfig.getPassword();
			
			sendHistoryEvent(inboundEvent, "Start pulling from " + branchName + "...", HistoryEvent.Type.INFO);
			connector.pull(git, username, password);
			sendHistoryEvent(inboundEvent, "Successfully pulled from " + branchName, HistoryEvent.Type.INFO);
		}
		catch(Exception e) {
			
			log.error("Pull error", e);
			state.setSuspensionStep(GitMergesActionSuspensionStep.CURRENT_MERGE_START);
			throw new ActionSuspensionException("Error pulling from " + branchName + ". Click Resume if you want to retry. Error was: " + e.getMessage(), state);
		}
	}
	
	private List<GitMerge> parseMerges(boolean pull, String mergesString) {
		
		List<GitMerge> merges = new ArrayList<>();
		
		String[] mergesStrings = mergesString.split(";");
		
		for(String mergeString: mergesStrings) {
			
			String[] mergeBranches = mergeString.split("->");
			
			if(mergeBranches.length < 2) {
				
				throw new BusinessException("Not enough branches defined in: " + mergeString);
			}
			
			for(int i = 1; i < mergeBranches.length; i++) {
				
				String sourceBranch = mergeBranches[i - 1];
				String targetBranch = mergeBranches[i];

				if(StringUtils.isBlank(sourceBranch) || StringUtils.isBlank(targetBranch)) {
					
					throw new BusinessException("Empty branch in: " + mergeString);
				}
				
				GitMerge merge = new GitMerge();
				merge.setPull(pull);
				merge.setSourceBranch(sourceBranch.trim());
				merge.setTargetBranch(targetBranch.trim());
				merges.add(merge);
			}
		}

		return merges;
	}

	private void verifyManualMerge(GitRepository git, GitMergesActionState state) {
		
		// For the moment, just check if the working tree is clean
		if(!connector.isWorkingTreeClean(git)) {
			
			state.setSuspensionStep(GitMergesActionSuspensionStep.CURRENT_MERGE_MANUAL);
			throw new ActionSuspensionException("Please resolve the merge manually and commit, leaving the working tree clean, and then click Resume", state);
		}
	}

	private String getActionDescription(GitMergesActionState state) {
		
		String folder = state.getFolder();
		String mergesString = state.getMergesString();
		boolean pull = state.isPull();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(START_LIST);
		
		builder.append(START_LIST_ELEMENT).append("Folder: ").append(folder).append(END_LIST_ELEMENT);
		
		builder.append(START_LIST_ELEMENT).append("Branches: ").append(mergesString).append(END_LIST_ELEMENT);
		
		builder.append(START_LIST_ELEMENT).append("Pull: ").append(pull ? "yes" : "no").append(END_LIST_ELEMENT);
		
		builder.append(END_LIST);
		
		return builder.toString();
	}
}
