package com.utils.releaseshelper.connector.maven;

import java.io.File;
import java.util.Map;

import com.utils.releaseshelper.model.error.MockException;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Maven connector, for test purposes
 */
@Slf4j
public class MavenConnectorMock implements MavenConnector {
	
	private int errorsToThrow = 0;
	
	private int thrownErrors = 0;
	
	@Override
	public File getPomFile(String projectFolder) {
		
		log.warn("Maven operations disabled: skipping get POM file");
		return null;
	}

	@Override
	public void runCommand(File pomFile, CommandLineOutputHandler outputHandler, String goals, Map<String, String> arguments, boolean offline) {
		
		log.warn("Maven operations disabled: skipping run command {} with arguments {}", goals, arguments);
		
		if(errorsToThrow > 0) {
			
			if(thrownErrors >= errorsToThrow) {
				
				thrownErrors = 0;
			}
			else {
				
				thrownErrors++;
				throw new MockException("This is a mock Maven error!");
			}
		}
	}
}
