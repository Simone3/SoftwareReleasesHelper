package com.utils.releaseshelper.connector.maven;

import java.io.File;
import java.util.Map;

import com.utils.releaseshelper.connector.Connector;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

/**
 * The connector to interact with a Maven project
 */
public interface MavenConnector extends Connector {

	File getPomFile(String projectFolder);

	void runCommand(File pomFile, CommandLineOutputHandler outputHandler, String goals, Map<String, String> arguments);
}
