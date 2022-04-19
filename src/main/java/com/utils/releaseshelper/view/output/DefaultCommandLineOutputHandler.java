package com.utils.releaseshelper.view.output;

import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultCommandLineOutputHandler implements CommandLineOutputHandler {

	private final CommandLineInterface cli;
	
	@Override
	public void printLine(String line) {
		
		if(line == null) {
			
			cli.println();
		}
		else {
			
			cli.println(line);
		}
	}
}
