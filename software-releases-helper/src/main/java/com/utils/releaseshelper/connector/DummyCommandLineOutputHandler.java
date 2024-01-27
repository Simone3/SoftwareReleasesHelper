package com.utils.releaseshelper.connector;

/**
 * A simple implementation of CommandLineOutputHandler that does nothing with the output
 */
public class DummyCommandLineOutputHandler implements CommandLineOutputHandler {

	@Override
	public void printLine(String line) {
		
		// Do nothing here
	}
}
