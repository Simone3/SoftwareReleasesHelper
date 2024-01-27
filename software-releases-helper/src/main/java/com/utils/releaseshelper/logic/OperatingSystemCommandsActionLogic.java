package com.utils.releaseshelper.logic;

import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST_ELEMENT;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST_ELEMENT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.utils.releaseshelper.connector.CommandLineOutputHandler;
import com.utils.releaseshelper.connector.DummyCommandLineOutputHandler;
import com.utils.releaseshelper.connector.git.GitConnector;
import com.utils.releaseshelper.connector.git.GitRepository;
import com.utils.releaseshelper.connector.process.OperatingSystemConnector;
import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.GitCommit;
import com.utils.releaseshelper.model.domain.GitConfig;
import com.utils.releaseshelper.model.domain.OperatingSystemCommand;
import com.utils.releaseshelper.model.domain.OperatingSystemCommandsAction;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InboundActionEvent;
import com.utils.releaseshelper.model.logic.OperatingSystemCommandsActionEvent;
import com.utils.releaseshelper.model.logic.OperatingSystemCommandsActionState;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.utils.VariableUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic component to run the Operating System Commands Action
 */
@Slf4j
@Component
public class OperatingSystemCommandsActionLogic extends ActionLogic {

	private final OperatingSystemConnector osConnector;
	private final GitConnector gitConnector;

	public OperatingSystemCommandsActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter, OperatingSystemConnector osConnector, GitConnector gitConnector) {
		
		super(globalContext, webSocketOutboundAdapter);
		this.osConnector = osConnector;
		this.gitConnector = gitConnector;
	}
	
	public void run(OperatingSystemCommandsActionEvent inboundEvent) {

		try {
			
			validateOperatingSystemCommandsEvent(inboundEvent);
			
			OperatingSystemCommandsActionState state = initializeState(inboundEvent);
			
			doBeforeCommands(inboundEvent, state);
			runCommands(inboundEvent, state);
			doAfterCommands(inboundEvent, state);
		}
		catch(Exception e) {
			
			log.error("OS commands error", e);
			sendHistoryEvent(inboundEvent, "Failed to run OS commands: " + e.getMessage(), HistoryEvent.Type.ERROR);
			sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.FAILURE);
		}
	}
	
	private void validateOperatingSystemCommandsEvent(OperatingSystemCommandsActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}

	private OperatingSystemCommandsActionState initializeState(OperatingSystemCommandsActionEvent inboundEvent) {
		
		// Get action definition and user inputs
		String actionName = inboundEvent.getActionName();
		OperatingSystemCommandsAction action = getAction(actionName, OperatingSystemCommandsAction.class);
		List<KeyValuePair> userVariableValues = inboundEvent.getVariableValues();
		String folder = action.getFolder();
		List<OperatingSystemCommand> commands = action.getCommands();
		GitCommit gitCommit = action.getGitCommit();
		
		// Parse variables for placeholders
		List<KeyValuePair> placeholderValues = getVariableValues(action, userVariableValues);
		
		// Replace placeholders, if any
		folder = VariableUtils.replacePlaceholders(folder, placeholderValues);
		commands = copyAndReplaceCommands(commands, placeholderValues);
		gitCommit = copyAndReplaceGitCommit(gitCommit, placeholderValues);
		
		// Build and return the action state
		OperatingSystemCommandsActionState state = new OperatingSystemCommandsActionState();
		state.setFolder(folder);
		state.setCommands(commands);
		state.setGitCommit(gitCommit);
		state.setCurrentCommandIndex(0);
		return state;
	}
	
	private List<OperatingSystemCommand> copyAndReplaceCommands(List<OperatingSystemCommand> sourceCommands, List<KeyValuePair> placeholderValues) {
		
		List<OperatingSystemCommand> commands = new ArrayList<>();
		for(OperatingSystemCommand sourceCommand: sourceCommands) {
			
			OperatingSystemCommand command = new OperatingSystemCommand();
			command.setCommand(VariableUtils.replacePlaceholders(sourceCommand.getCommand(), placeholderValues));
			command.setSuppressOutput(sourceCommand.isSuppressOutput());
			commands.add(command);
		}
		return commands;
	}
	
	private GitCommit copyAndReplaceGitCommit(GitCommit sourceGitCommit, List<KeyValuePair> placeholderValues) {
		
		if(sourceGitCommit == null) {
			
			return null;
		}
		
		GitCommit gitCommit = new GitCommit();
		gitCommit.setBranch(VariableUtils.replacePlaceholders(sourceGitCommit.getBranch(), placeholderValues));
		gitCommit.setPull(sourceGitCommit.isPull());
		gitCommit.setMessage(VariableUtils.replacePlaceholders(sourceGitCommit.getMessage(), placeholderValues));
		return gitCommit;
	}
	
	private void doBeforeCommands(InboundActionEvent inboundEvent, OperatingSystemCommandsActionState state) {
		
		// If requested, prepare the Git repository
		if(state.getGitCommit() != null) {
			
			prepareGitRepository(inboundEvent, state);
		}
	}
	
	private void runCommands(InboundActionEvent inboundEvent, OperatingSystemCommandsActionState state) {
		
		String folderPath = state.getFolder();
		File folder = osConnector.getCommandFolder(folderPath);
		List<OperatingSystemCommand> commands = state.getCommands();
		
		for(OperatingSystemCommand command: commands) {
			
			String commandValue = command.getCommand();
			boolean suppressOutput = command.isSuppressOutput();
			
			CommandLineOutputHandler outputHandler = suppressOutput ? new DummyCommandLineOutputHandler() : line -> sendHistoryEvent(inboundEvent, line, HistoryEvent.Type.INFO);
		
			sendHistoryEvent(inboundEvent, "Start running \"" + commandValue + "\" command...", HistoryEvent.Type.INFO);
			
			int statusCode = osConnector.runCommand(folder, commandValue, outputHandler);
			if(statusCode != 0) {
				
				throw new BusinessException("Command error, status code is " + statusCode);
			}
			
			sendHistoryEvent(inboundEvent, "Command \"" + commandValue + "\" successfully completed!", HistoryEvent.Type.INFO);
		}
	}
	
	private void doAfterCommands(InboundActionEvent inboundEvent, OperatingSystemCommandsActionState state) {
		
		// If requested, commit any changes to the Git repository
		if(state.getGitCommit() != null) {
			
			commitGitChanges(inboundEvent, state);
		}
		
		// Send the final OK message
		sendHistoryEvent(inboundEvent, "All commands completed!\n\n" + getActionDescription(state), HistoryEvent.Type.SUCCESS);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUCCESS);
	}

	private void prepareGitRepository(InboundActionEvent inboundEvent, OperatingSystemCommandsActionState state) {
		
		GitCommit gitCommit = state.getGitCommit();
		String repositoryFolder = state.getFolder();
		String branch = gitCommit.getBranch();
		boolean pull = gitCommit.isPull();
		
		try(GitRepository git = gitConnector.getRepository(repositoryFolder)) {
			
			// Check that the branch actually exists
			Set<String> nonExistingBranches = gitConnector.checkNonExistingBranches(git, Set.of(branch));
			if(!nonExistingBranches.isEmpty()) {
				
				throw new BusinessException("The branch \"" + branch + "\" does not exist in your local Git repository");
			}
			
			// Check that the working tree is clean
			if(!gitConnector.isWorkingTreeClean(git)) {
				
				throw new BusinessException("Git working tree is not clean, please commit or discard changed files");
			}
			
			// Save the original branch for later use
			String originalBranch = gitConnector.getCurrentBranch(git);
			state.setOriginalGitBranch(originalBranch);
			
			// Switch to the requested branch
			gitConnector.switchBranch(git, branch);
			sendHistoryEvent(inboundEvent, "Switched to Git branch " + branch, HistoryEvent.Type.INFO);
			
			// Pull the current branch if necessary
			if(pull) {
				
				GitConfig gitConfig = globalContext.getDomainModel().getConfig().getGit();
				
				String username = gitConfig.getUsername();
				String password = gitConfig.getPassword();
				
				sendHistoryEvent(inboundEvent, "Start pulling from Git branch " + branch + "...", HistoryEvent.Type.INFO);
				gitConnector.pull(git, username, password);
				sendHistoryEvent(inboundEvent, "Successfully pulled from Git branch " + branch, HistoryEvent.Type.INFO);
			}
		}
	}

	private void commitGitChanges(InboundActionEvent inboundEvent, OperatingSystemCommandsActionState state) {
		
		GitCommit gitCommit = state.getGitCommit();
		String repositoryFolder = state.getFolder();
		String originalBranch = state.getOriginalGitBranch();
		String message = gitCommit.getMessage();
		
		try(GitRepository git = gitConnector.getRepository(repositoryFolder)) {
			
			String currentBranch = gitConnector.getCurrentBranch(git);
			
			// Commit all changed files, if any
			if(gitConnector.isWorkingTreeClean(git)) {
				
				sendHistoryEvent(inboundEvent, "Nothing to commit", HistoryEvent.Type.INFO);
				state.setActuallyGitCommitted(false);
			}
			else {
				
				gitConnector.addAll(git);
				gitConnector.commit(git, message);
				state.setActuallyGitCommitted(true);
			    
				sendHistoryEvent(inboundEvent, "Successfully committed all changes on Git branch " + currentBranch + " with comment \"" + message + "\"", HistoryEvent.Type.INFO);
			}
			
			// Switch back to the original branch
			gitConnector.switchBranch(git, originalBranch);
			sendHistoryEvent(inboundEvent, "Switched to Git branch " + originalBranch + " (original branch)", HistoryEvent.Type.INFO);
		}
	}

	private String getActionDescription(OperatingSystemCommandsActionState state) {

		String folder = state.getFolder();
		List<OperatingSystemCommand> commands = state.getCommands();
		GitCommit gitCommit = state.getGitCommit();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(START_LIST);
		
		builder.append(START_LIST_ELEMENT).append("Folder: ").append(folder).append(END_LIST_ELEMENT);
		
		builder.append(START_LIST_ELEMENT).append("Commands:");
		builder.append(START_LIST);
		for(OperatingSystemCommand command: commands) {
			
			builder.append(START_LIST_ELEMENT).append(command.getCommand()).append(END_LIST_ELEMENT);
		}
		builder.append(END_LIST).append(END_LIST_ELEMENT);
		
		builder.append(START_LIST_ELEMENT).append("Git commit:");
		if(gitCommit == null) {
			
			builder.append(" none");
		}
		else {
			
			builder.append(START_LIST);
			builder.append(START_LIST_ELEMENT).append("Branch: ").append(gitCommit.getBranch()).append(END_LIST_ELEMENT);
			builder.append(START_LIST_ELEMENT).append("Message: ").append(gitCommit.getMessage()).append(END_LIST_ELEMENT);
			builder.append(START_LIST_ELEMENT).append("Pull: ").append(gitCommit.isPull() ? "yes" : "no").append(END_LIST_ELEMENT);
			builder.append(END_LIST);
		}
		builder.append(END_LIST_ELEMENT);
		
		builder.append(END_LIST);
		
		return builder.toString();
	}
}
