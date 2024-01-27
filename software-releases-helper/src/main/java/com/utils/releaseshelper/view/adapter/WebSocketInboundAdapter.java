package com.utils.releaseshelper.view.adapter;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.utils.releaseshelper.logic.CancelActionLogic;
import com.utils.releaseshelper.logic.GitMergesActionLogic;
import com.utils.releaseshelper.logic.InitSessionLogic;
import com.utils.releaseshelper.logic.JenkinsBuildActionLogic;
import com.utils.releaseshelper.logic.OperatingSystemCommandsActionLogic;
import com.utils.releaseshelper.model.logic.CancelActionEvent;
import com.utils.releaseshelper.model.logic.GitMergesActionEvent;
import com.utils.releaseshelper.model.logic.GitMergesResumeActionEvent;
import com.utils.releaseshelper.model.logic.InitSessionEvent;
import com.utils.releaseshelper.model.logic.JenkinsBuildActionEvent;
import com.utils.releaseshelper.model.logic.OperatingSystemCommandsActionEvent;

import lombok.RequiredArgsConstructor;

/**
 * Spring Controller to receive inbound WebSocket events
 */
@Controller
@RequiredArgsConstructor
public class WebSocketInboundAdapter implements ViewAdapter {
	
	private final InitSessionLogic initSessionLogic;
	private final CancelActionLogic cancelActionLogic;
	private final JenkinsBuildActionLogic jenkinsBuildActionLogic;
	private final GitMergesActionLogic gitMergesActionLogic;
	private final OperatingSystemCommandsActionLogic operatingSystemCommandsActionLogic;
	
	@MessageMapping("/session/init/run")
	private void onRunInitSession(InitSessionEvent inboundEvent) {
		
		initSessionLogic.run(inboundEvent);
	}
	
	@MessageMapping("/action/cancel")
	private void onResumeGitMergesAction(CancelActionEvent inboundEvent) {
		
		cancelActionLogic.run(inboundEvent);
	}

	@MessageMapping("/action/jenkins/build/run")
	private void onRunJenkinsBuildAction(JenkinsBuildActionEvent inboundEvent) {
		
		jenkinsBuildActionLogic.run(inboundEvent);
	}
	
	@MessageMapping("/action/git/merge/run")
	private void onRunGitMergesAction(GitMergesActionEvent inboundEvent) {
		
		gitMergesActionLogic.run(inboundEvent);
	}
	
	@MessageMapping("/action/git/merge/resume")
	private void onResumeGitMergesAction(GitMergesResumeActionEvent inboundEvent) {
		
		gitMergesActionLogic.resume(inboundEvent);
	}
	
	@MessageMapping("/action/os/commands/run")
	private void onRunOsCommandsAction(OperatingSystemCommandsActionEvent inboundEvent) {
		
		operatingSystemCommandsActionLogic.run(inboundEvent);
	}
}
