package com.utils.releaseshelper.service.process;

import java.io.File;
import java.util.List;

import com.utils.releaseshelper.connector.process.OperatingSystemConnector;
import com.utils.releaseshelper.connector.process.OperatingSystemConnectorMock;
import com.utils.releaseshelper.connector.process.OperatingSystemConnectorReal;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.service.process.OperatingSystemCommandServiceModel;
import com.utils.releaseshelper.model.service.process.OperatingSystemRunCommandServiceInput;
import com.utils.releaseshelper.service.Service;
import com.utils.releaseshelper.utils.RetryUtils;
import com.utils.releaseshelper.view.CommandLineInterface;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;
import com.utils.releaseshelper.view.output.DefaultCommandLineOutputHandler;
import com.utils.releaseshelper.view.output.DummyCommandLineOutputHandler;

/**
 * A Service that allows to operate on the generic operating system command line
 */
public class OperatingSystemService implements Service {

	private final CommandLineInterface cli;
	private final OperatingSystemConnector connector;

	public OperatingSystemService(Config config, CommandLineInterface cli) {
		
		this.cli = cli;
		this.connector = config.isTestMode() ? new OperatingSystemConnectorMock() : new OperatingSystemConnectorReal();
	}
	
	public void runCommands(OperatingSystemRunCommandServiceInput input) {
		
		File folder = connector.getCommandFolder(input.getFolder());
		
		List<OperatingSystemCommandServiceModel> commands = input.getCommands();
		
		for(int i = 0; i < commands.size(); i++) {
			
			if(i != 0) {
				
				cli.println();
			}
			
			runCommandWithRetries(folder, commands.get(i));
		}
	}
	
	private boolean runCommandWithRetries(File folder, OperatingSystemCommandServiceModel command) {
		
		String commandValue = command.getCommand();
		boolean suppressOutput = command.isSuppressOutput();
		
		CommandLineOutputHandler outputHandler = suppressOutput ? new DummyCommandLineOutputHandler() : new DefaultCommandLineOutputHandler(cli);
		
		boolean commandSuccess = RetryUtils.retry(cli, "Retry command", "Cannot run command", () -> {
			
			cli.println("Start running \"%s\" command...", commandValue);
			
			int statusCode = connector.runCommand(folder, commandValue, outputHandler);
			if(statusCode != 0) {
				
				throw new IllegalStateException("Command error, status code is " + statusCode);
			}
			
			cli.println("Command \"%s\" successfully completed!", commandValue);
		});
		
		if(!commandSuccess) {
			
			cli.println("Command skipped");
		}

		return commandSuccess;
	}
}
