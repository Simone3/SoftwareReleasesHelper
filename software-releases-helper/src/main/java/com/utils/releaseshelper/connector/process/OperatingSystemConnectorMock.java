package com.utils.releaseshelper.connector.process;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import com.utils.releaseshelper.connector.CommandLineOutputHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the operating system connector, for test purposes
 */
@Slf4j
public class OperatingSystemConnectorMock implements OperatingSystemConnector {
	
	private int errorProbability = 0;
	
	private long delay = 1000l;

	@Override
	public File getCommandFolder(String folderPath) {
		
		log.warn("Operating system invocations disabled: skipping get folder {}", folderPath);
		return null;
	}
	
	@Override
	public int runCommand(File folder, String command, CommandLineOutputHandler outputHandler) {
		
		log.warn("Operating system invocations disabled: skipping run command {}", command);
		
		if(delay > 0) {
			
			try {
				
				Thread.sleep(delay);
			}
			catch(InterruptedException e) {
				
				Thread.currentThread().interrupt();
			}
		}

		if(errorProbability > 0 && ThreadLocalRandom.current().nextInt(1, 101) <= errorProbability) {
			
			return 1;
		}
		
		return 0;
	}
}
