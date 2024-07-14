package com.utils.releaseshelper.connector.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.SystemUtils;

import com.utils.releaseshelper.connector.CommandLineOutputHandler;
import com.utils.releaseshelper.model.error.BusinessException;

/**
 * An implementation of the operating system connector based on the Java Runtime/Process classes
 */
public class OperatingSystemConnectorReal implements OperatingSystemConnector {

	@Override
	public File getCommandFolder(String folderPath) {
		
		File folder = new File(folderPath);
		
		if(!folder.exists()) {
			
			throw new BusinessException("Folder " + folder.getAbsolutePath() + " does not exist!");
		}
		
		if(!folder.isDirectory()) {
			
			throw new BusinessException(folder.getAbsolutePath() + " is a file!");
		}
		
		return folder;
	}
	
	@Override
	public int runCommand(File folder, String command, CommandLineOutputHandler outputHandler) {

		ProcessBuilder builder;
		if(SystemUtils.IS_OS_WINDOWS) {

			builder = new ProcessBuilder("cmd", "/c", command);
		}
		else {
			
			builder = new ProcessBuilder("sh", "-c", command);
		}
		
		builder.directory(folder);

		try {
			
			Process process = builder.start();
	
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	
			String line;
			while((line = input.readLine()) != null) {
	
				outputHandler.printLine(line);
			}
	
			return process.waitFor();
		}
		catch(InterruptedException e) {
			
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch(IOException e) {
			
			throw new BusinessException(e.getMessage(), e);
		}
	}
}
