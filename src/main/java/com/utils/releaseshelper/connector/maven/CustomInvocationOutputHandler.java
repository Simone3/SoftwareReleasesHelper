package com.utils.releaseshelper.connector.maven;

import java.io.IOException;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CustomInvocationOutputHandler implements InvocationOutputHandler {
	
	private final CommandLineOutputHandler handler;
	
	@Override
	public void consumeLine(String line) throws IOException {
		
		handler.printLine(line);
	}
}