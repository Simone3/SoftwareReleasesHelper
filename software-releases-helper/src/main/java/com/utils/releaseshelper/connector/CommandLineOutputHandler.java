package com.utils.releaseshelper.connector;

/**
 * An interface used by connectors that print CLI output
 */
public interface CommandLineOutputHandler {

	void printLine(String line);
}
