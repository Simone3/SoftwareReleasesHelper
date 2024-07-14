package com.utils.releaseshelper.logic;

import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST_ELEMENT;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST_ELEMENT;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.PullResult;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitRepository;
import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.GitPullAllAction;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.error.GitUnauthorizedException;
import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.GitPullAllActionEvent;
import com.utils.releaseshelper.model.logic.GitPullAllActionState;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InboundActionEvent;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.utils.VariableUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic component to run the Git Pull All Action
 */
@Slf4j
@Component
public class GitPullAllActionLogic extends ActionLogic {
	
	private final GitConnector connector;

	public GitPullAllActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter, GitConnector connector) {
		
		super(globalContext, webSocketOutboundAdapter);
		this.connector = connector;
	}

	public void run(GitPullAllActionEvent inboundEvent) {
		
		try {
			
			validateRunEvent(inboundEvent);
			
			GitPullAllActionState state = initializeState(inboundEvent);
			String parentFolderPath = state.getParentFolder();
			
			GitConfig gitConfig = globalContext.getDomainModel().getConfig().getGit();
			
			// Pull all sub-folders of the parent folder
			File parentFolder = getParentFolder(parentFolderPath);
			sendHistoryEvent(inboundEvent, "Start pulling all sub-folders of " + parentFolder.getAbsolutePath(), HistoryEvent.Type.INFO);
			pullAllSubFolders(inboundEvent, state, gitConfig, parentFolder);

			// Send the final message
			if(state.getPulledRepos() > 0) {
				
				if(state.getDirtyRepos() + state.getErrorRepos() == 0) {
					
					sendHistoryEvent(inboundEvent, "All sub-folders successfully pulled!" + getActionDescription(state), HistoryEvent.Type.SUCCESS);
					sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUCCESS);
				}
				else {
					
					sendHistoryEvent(inboundEvent, "Sub-folders pulled but there were some failures." + getActionDescription(state), HistoryEvent.Type.WARNING);
					sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUCCESS);
				}
			}
			else {
				
				sendHistoryEvent(inboundEvent, "No sub-folders successfully pulled." + getActionDescription(state), HistoryEvent.Type.ERROR);
				sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.FAILURE);
			}
		}
		catch(Exception e) {
			
			handleGenericError(inboundEvent, e);
		}
	}

	private File getParentFolder(String parentFolderPath) {
		
		File parentFolder = new File(parentFolderPath);
		
		if(!parentFolder.exists()) {
			
			throw new BusinessException("Parent folder " + parentFolderPath + " does not exist!");
		}
		
		if(!parentFolder.isDirectory()) {
			
			throw new BusinessException(parentFolderPath + " is not a folder!");
		}
		
		return parentFolder;
	}

	private void pullAllSubFolders(GitPullAllActionEvent inboundEvent, GitPullAllActionState state, GitConfig gitConfig, File parentFolder) {
		
		List<File> folderStack = new ArrayList<>();
		folderStack.add(parentFolder);
		
		while(!folderStack.isEmpty()) {
			
			File currentFolder = folderStack.remove(folderStack.size() - 1);
			
			File gitFolder = new File(currentFolder.getAbsolutePath() + File.separator + ".git");
			if(gitFolder.exists()) {
				
				boolean continueTraversal = pullGitRepo(inboundEvent, state, gitConfig, currentFolder);
				if(!continueTraversal) {
					
					break;
				}
			}
			else {
			
				File[] children = currentFolder.listFiles();
				Arrays.sort(children);
				
				for(int i = children.length - 1; i >= 0; i--) {
					
					if(children[i].isDirectory()) {
						
						folderStack.add(children[i]);
					}
				}
			}
		}
	}

	private boolean pullGitRepo(GitPullAllActionEvent inboundEvent, GitPullAllActionState state, GitConfig gitConfig, File currentFolder) {
		
		try(GitRepository git = connector.getRepository(currentFolder)) {
			
			if(!state.isSkipIfWorkingTreeDirty() || connector.isWorkingTreeClean(git)) {

				String username = gitConfig.getUsername();
				String password = gitConfig.getPassword();
				String currentBranch = connector.getCurrentBranch(git);
				PullResult pullResult = connector.pull(git, username, password);
				state.incrementPulledRepos();
				sendHistoryEvent(inboundEvent, "Successfully pulled " + currentFolder.getAbsolutePath() + " from " + pullResult.getFetchedFrom() + "/" + currentBranch, HistoryEvent.Type.INFO);
				return true;
			}
			else {
				
				state.incrementDirtyRepos();
				sendHistoryEvent(inboundEvent, "Skipped pulling " + currentFolder.getAbsolutePath() + " because of dirty working tree, commit or discard your changes", HistoryEvent.Type.WARNING);
				return true;
			}
		}
		catch(GitUnauthorizedException e) {
			
			log.error("Pull authentication error", e);
			state.incrementErrorRepos();
			sendHistoryEvent(inboundEvent, "Error pulling " + currentFolder.getAbsolutePath() + ": " + e.getMessage() + ". Stopping sub-folder traversal to avoid too many authentication errors", HistoryEvent.Type.ERROR);
			return false;
		}
		catch(Exception e) {
			
			log.error("Pull generic error", e);
			state.incrementErrorRepos();
			sendHistoryEvent(inboundEvent, "Error pulling " + currentFolder.getAbsolutePath() + ": " + e.getMessage(), HistoryEvent.Type.ERROR);
			return true;
		}
	}
	
	private void validateRunEvent(GitPullAllActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}
	
	private void handleGenericError(InboundActionEvent inboundEvent, Exception e) {
		
		log.error("Git merges error", e);
		sendHistoryEvent(inboundEvent, "Failed to Git pull all sub-folders: " + e.getMessage(), HistoryEvent.Type.ERROR);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.FAILURE);
	}

	private GitPullAllActionState initializeState(InboundActionEvent inboundEvent) {
		
		// Get action definition and user inputs
		String actionName = inboundEvent.getActionName();
		GitPullAllAction action = getAction(actionName, GitPullAllAction.class);
		List<KeyValuePair> userVariableValues = inboundEvent.getVariableValues();
		String parentFolder = action.getParentFolder();
		boolean skipIfWorkingTreeDirty = action.isSkipIfWorkingTreeDirty();
		
		// Parse variables for placeholders
		List<KeyValuePair> placeholderValues = getVariableValues(action, userVariableValues);
		
		// Replace placeholders, if any
		parentFolder = VariableUtils.replacePlaceholders(parentFolder, placeholderValues);
		
		// Build and return the action state
		GitPullAllActionState state = new GitPullAllActionState();
		state.setParentFolder(parentFolder);
		state.setSkipIfWorkingTreeDirty(skipIfWorkingTreeDirty);
		return state;
	}
	
	private String getActionDescription(GitPullAllActionState state) {
		
		String parentFolder = state.getParentFolder();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(START_LIST);
		
		builder.append(START_LIST_ELEMENT).append("Parent folder: ").append(parentFolder).append(END_LIST_ELEMENT);
		builder.append(START_LIST_ELEMENT).append("Pulled repositories: ").append(state.getPulledRepos()).append(END_LIST_ELEMENT);
		
		if(state.isSkipIfWorkingTreeDirty()) {
			
			builder.append(START_LIST_ELEMENT).append("Skipped repositories (dirty working tree): ").append(state.getDirtyRepos()).append(END_LIST_ELEMENT);
		}
		
		builder.append(START_LIST_ELEMENT).append("Failed repositories: ").append(state.getErrorRepos()).append(END_LIST_ELEMENT);
		
		builder.append(END_LIST);
		
		return builder.toString();
	}
}
