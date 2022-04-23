package com.utils.releaseshelper.connector.maven;

import java.io.File;
import java.util.Map;

import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Maven connector, for test purposes
 */
@Slf4j
public class MavenConnectorMock implements MavenConnector {
	
	private static final int ERRORS = 1;
	private int errorsCounter = 0;
	
	@Override
	public File getPomFile(String projectFolder) {
		
		log.warn("Maven operations disabled: skipping get POM file");
		return null;
	}

	@Override
	public void runCommand(File pomFile, CommandLineOutputHandler outputHandler, String goals, Map<String, String> arguments, boolean offline) {
		
		log.warn("Maven operations disabled: skipping run command {} with arguments {}", goals, arguments);
		
		if(ERRORS > 0) {
			
			if(errorsCounter >= ERRORS) {
				
				errorsCounter = 0;
			}
			else {
				
				errorsCounter++;
				throw new IllegalStateException("This is a mock Maven error!");
			}
		}
	}
}
