package com.utils.releaseshelper.service.main;

import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.logic.ActionFlags;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.service.git.GitService;
import com.utils.releaseshelper.service.jenkins.JenkinsService;
import com.utils.releaseshelper.service.maven.MavenService;
import com.utils.releaseshelper.service.process.OperatingSystemService;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.Getter;

/**
 * A helper container for all services
 */
@Getter
public class MainService implements Service {

	private final GitService git;
	private final JenkinsService jenkins;
	private final MavenService maven;
	private final OperatingSystemService operatingSystem;

	public MainService(Config config, ActionFlags actionFlags, CommandLineInterface cli) {
		
		git = actionFlags.isGitActions() ? new GitService(config, cli) : null;
		jenkins = actionFlags.isJenkinsActions() ? new JenkinsService(config, cli) : null;
		maven = actionFlags.isMavenActions() ? new MavenService(config, cli) : null;
		operatingSystem = actionFlags.isOperatingSystemActions() ? new OperatingSystemService(config, cli) : null;
	}
}
