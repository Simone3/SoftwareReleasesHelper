package com.utils.releaseshelper.connector.process;

import java.io.File;

import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the operating system connector, for test purposes
 */
@Slf4j
public class OperatingSystemConnectorMock implements OperatingSystemConnector {
	
	private int errorsToThrow = 0;
	private int thrownErrors = 0;

	@Override
	public File getCommandFolder(String folderPath) {
		
		log.warn("Operating system invocations disabled: skipping get folder {}", folderPath);
		return null;
	}
	
	@Override
	public int runCommand(File folder, String command, CommandLineOutputHandler outputHandler) {
		
		log.warn("Operating system invocations disabled: skipping run command {}", command);
		
		if(errorsToThrow > 0) {
			
			if(thrownErrors >= errorsToThrow) {
				
				thrownErrors = 0;
				return 0;
			}
			else {
				
				thrownErrors++;
				return 1;
			}
		}
		else {
			
			return 0;
		}
	}
}
