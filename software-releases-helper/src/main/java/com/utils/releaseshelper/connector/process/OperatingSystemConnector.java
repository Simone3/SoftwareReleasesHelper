package com.utils.releaseshelper.connector.process;

import java.io.File;

import com.utils.releaseshelper.connector.CommandLineOutputHandler;
import com.utils.releaseshelper.connector.Connector;

/**
 * The connector to run generic operating system commands
 */
public interface OperatingSystemConnector extends Connector {

	File getCommandFolder(String folderPath);
	
	int runCommand(File folder, String command, CommandLineOutputHandler outputHandler);
}
