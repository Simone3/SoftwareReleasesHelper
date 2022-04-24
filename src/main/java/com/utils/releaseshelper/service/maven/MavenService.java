package com.utils.releaseshelper.service.maven;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.connector.maven.MavenConnector;
import com.utils.releaseshelper.connector.maven.MavenConnectorMock;
import com.utils.releaseshelper.connector.maven.MavenConnectorReal;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.service.maven.MavenCommandServiceModel;
import com.utils.releaseshelper.model.service.maven.MavenRunCommandServiceInput;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.utils.RetryUtils;
import com.utils.releaseshelper.view.CommandLineInterface;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;
import com.utils.releaseshelper.view.output.DefaultCommandLineOutputHandler;
import com.utils.releaseshelper.view.output.DummyCommandLineOutputHandler;

/**
 * A Service that allows to operate on a Maven project
 */
public class MavenService implements Service {

	private final CommandLineInterface cli;
	private final MavenConnector connector;

	public MavenService(Config config, CommandLineInterface cli) {
		
		this.cli = cli;
		this.connector = config.isTestMode() ? new MavenConnectorMock() : new MavenConnectorReal(config.getMaven());
	}
	
	public void runCommands(MavenRunCommandServiceInput input) {
		
		String projectFolder = input.getProjectFolder();
		List<MavenCommandServiceModel> commands = input.getCommands();

		File pomFile = connector.getPomFile(projectFolder);
		
		for(int i = 0; i < commands.size(); i++) {
			
			if(i != 0) {
				
				cli.println();
			}
			
			runCommandWithRetries(pomFile, commands.get(i));
		}
	}
	
	private boolean runCommandWithRetries(File pomFile, MavenCommandServiceModel command) {
		
		String goals = command.getGoals();
		Map<String, String> arguments = command.getArguments();
		boolean offline = command.isOffline();
		boolean suppressOutput = command.isSuppressOutput();
		
		CommandLineOutputHandler outputHandler = suppressOutput ? new DummyCommandLineOutputHandler() : new DefaultCommandLineOutputHandler(cli);
		
		boolean commandSuccess = RetryUtils.retry(cli, "Retry command", "Cannot run command", () -> {
			
			cli.println("Start running \"%s\" command...", goals);
			connector.runCommand(pomFile, outputHandler, goals, arguments, offline);
			cli.println("Command \"%s\" successfully completed!", goals);
		});
		
		if(!commandSuccess) {
			
			cli.println("Command skipped");
		}

		return commandSuccess;
	}
}
