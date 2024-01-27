package com.utils.releaseshelper.logic;

import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.END_LIST_ELEMENT;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST;
import static com.utils.releaseshelper.model.view.cli.MessageFormatPlaceholders.START_LIST_ELEMENT;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.connector.jenkins.JenkinsConnector;
import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.model.domain.JenkinsBuildAction;
import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.logic.ActionStatusEvent;
import com.utils.releaseshelper.model.logic.HistoryEvent;
import com.utils.releaseshelper.model.logic.InboundActionEvent;
import com.utils.releaseshelper.model.logic.JenkinsBuildActionEvent;
import com.utils.releaseshelper.model.logic.JenkinsBuildActionState;
import com.utils.releaseshelper.model.logic.JenkinsCrumbData;
import com.utils.releaseshelper.model.misc.KeyValuePair;
import com.utils.releaseshelper.utils.VariableUtils;
import com.utils.releaseshelper.view.adapter.WebSocketOutboundAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Logic component to run the Jenkins Build Action
 */
@Slf4j
@Component
public class JenkinsBuildActionLogic extends ActionLogic {

	private final JenkinsConnector connector;
	
	public JenkinsBuildActionLogic(GlobalContext globalContext, WebSocketOutboundAdapter webSocketOutboundAdapter, JenkinsConnector connector) {
		
		super(globalContext, webSocketOutboundAdapter);
		this.connector = connector;
	}
	
	public void run(JenkinsBuildActionEvent inboundEvent) {

		try {
			
			validateRunEvent(inboundEvent);
			
			JenkinsBuildActionState state = initializeState(inboundEvent);
			
			startBuild(inboundEvent, state);
		}
		catch(Exception e) {
			
			log.error("Jenkins build error", e);
			sendHistoryEvent(inboundEvent, "Failed to start Jenkins build: " + e.getMessage(), HistoryEvent.Type.ERROR);
			sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.FAILURE);
		}
	}

	private void validateRunEvent(JenkinsBuildActionEvent inboundEvent) {
		
		validateInboundActionEvent(inboundEvent);
	}
	
	private JenkinsBuildActionState initializeState(InboundActionEvent inboundEvent) {
		
		// Get action definition and user inputs
		String actionName = inboundEvent.getActionName();
		JenkinsBuildAction action = getAction(actionName, JenkinsBuildAction.class);
		List<KeyValuePair> userVariableValues = inboundEvent.getVariableValues();
		String url = action.getUrl();
		List<KeyValuePair> parameters = action.getParameters();
		
		// Parse variables for placeholders
		List<KeyValuePair> placeholderValues = getVariableValues(action, userVariableValues);
		
		// Replace placeholders, if any
		url = VariableUtils.replacePlaceholders(url, placeholderValues);
		parameters = copyAndReplaceParameters(parameters, placeholderValues);
		
		// Build and return the action state
		JenkinsBuildActionState state = new JenkinsBuildActionState();
		state.setUrl(url);
		state.setParameters(parameters);
		return state;
	}
	
	private List<KeyValuePair> copyAndReplaceParameters(List<KeyValuePair> sourceParameters, List<KeyValuePair> placeholderValues) {
		
		if(CollectionUtils.isEmpty(sourceParameters)) {
			
			return List.of();
		}
		
		List<KeyValuePair> parameters = new ArrayList<>();
		for(KeyValuePair sourceParameter: sourceParameters) {
			
			String key = sourceParameter.getKey();
			String value = VariableUtils.replacePlaceholders(sourceParameter.getValue(), placeholderValues);
			parameters.add(new KeyValuePair(key, value));
		}
		return parameters;
	}
	
	public void startBuild(JenkinsBuildActionEvent inboundEvent, JenkinsBuildActionState state) {
		
		String url = state.getUrl();
		List<KeyValuePair> parameters = state.getParameters();
		
		JenkinsConfig jenkinsConfig = globalContext.getDomainModel().getConfig().getJenkins();
		String username = jenkinsConfig.getUsername();
		String password = jenkinsConfig.getPassword();
		
		// If necessary, retrieve the crumb data
		JenkinsCrumbData crumbData = getCrumbData(inboundEvent);
		
		// Start the actual build
		sendHistoryEvent(inboundEvent, "Starting Jenkins build...", HistoryEvent.Type.INFO);		
		connector.startBuild(url, username, password, crumbData, parameters);
		
		// Send the final OK message
		sendHistoryEvent(inboundEvent, "Jenkins build started!" + getActionDescription(state), HistoryEvent.Type.SUCCESS);
		sendActionStatusEvent(inboundEvent, ActionStatusEvent.Type.SUCCESS);
	}
	
	private JenkinsCrumbData getCrumbData(JenkinsBuildActionEvent inboundEvent) {
		
		JenkinsConfig jenkinsConfig = globalContext.getDomainModel().getConfig().getJenkins();
		boolean useCrumb = jenkinsConfig.isUseCrumb();
		String crumbUrl = jenkinsConfig.getCrumbUrl();
		String username = jenkinsConfig.getUsername();
		String password = jenkinsConfig.getPassword();
		
		JenkinsCrumbData crumbData = null;
		
		if(useCrumb) {
			
			crumbData = globalContext.getGlobalState().getJenkinsCrumb(username);
			if(crumbData == null) {
				
				sendHistoryEvent(inboundEvent, "Getting Jenkins crumb...", HistoryEvent.Type.INFO);
				
				crumbData = connector.getCrumb(crumbUrl, username, password);
				globalContext.getGlobalState().putJenkinsCrumb(username, crumbData);
				
				sendHistoryEvent(inboundEvent, "Got Jenkins crumb: " + crumbData.getCrumb(), HistoryEvent.Type.INFO);
			}
		}
		
		return crumbData;
	}

	private String getActionDescription(JenkinsBuildActionState state) {
		
		String url = state.getUrl();
		List<KeyValuePair> parameters = state.getParameters();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(START_LIST);
		
		builder.append(START_LIST_ELEMENT).append("URL: ").append(url).append(END_LIST_ELEMENT);
		
		builder.append(START_LIST_ELEMENT).append("Parameters:");
		if(parameters.isEmpty()) {
		
			builder.append(" none");
		}
		else {

			builder.append(START_LIST);
			for(KeyValuePair parameter: parameters) {
				
				builder.append(START_LIST_ELEMENT).append(parameter.getKey()).append(": ").append(parameter.getValue()).append(END_LIST_ELEMENT);
			}
			builder.append(END_LIST);
		}
		builder.append(END_LIST_ELEMENT);
		
		builder.append(END_LIST);
		
		return builder.toString();
	}
}
