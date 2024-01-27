package com.utils.releaseshelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.context.GlobalContext;
import com.utils.releaseshelper.view.userinterface.CommandLineInterface;

import lombok.RequiredArgsConstructor;

/**
 * A CLI runner to print startup messages
 */
@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

	private final CommandLineInterface cli;
	private final GlobalContext globalContext;
	
	@Value("${server.port:}")
	private Integer webPort;
	
	@Override
	public void run(String... args) throws Exception {
		
		if(globalContext.getDomainModel().getConfig().isWebGui()) {
			
			// Print web GUI URL
			cli.printLine("All set! Open http://localhost:%s to start. Do NOT close this terminal window.", webPort);
		}
		else {
			
			throw new IllegalStateException("Web GUI is at the moment the only mode");
		}
	}
}
