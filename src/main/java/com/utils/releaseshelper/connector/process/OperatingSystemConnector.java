package com.utils.releaseshelper.connector.process;

import java.io.File;

import com.utils.releaseshelper.connector.Connector;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

/**
 * The connector to run generic operating system commands
 */
public interface OperatingSystemConnector extends Connector {

	File getCommandFolder(String folderPath);
	
	int runCommand(File folder, String command, CommandLineOutputHandler outputHandler);
}
