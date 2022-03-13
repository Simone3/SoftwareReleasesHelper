package com.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.utils.logic.main.MainLogic;
import com.utils.logic.validation.Validator;
import com.utils.model.properties.Properties;
import com.utils.view.CommandLineInterface;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Runner implements CommandLineRunner {
	
	@Autowired
	private Properties properties;
	
	private final CommandLineInterface cli;
	private final Validator validator;
	
	public Runner() {
		
		cli = new CommandLineInterface();
		validator = new Validator();
	}

	@Override
	public void run(String... args) throws Exception {
		
		if(processProperties()) {
			
			startMainLogic();
		}
	}

	private boolean processProperties() {
		
		try {
			
			validator.validateProperties(properties);
			return true;
		}
		catch(Exception e) {
			
			cli.printError("Invalid configuration: %s", e.getMessage());
			log.error("Validation error", e);
			printSampleConfiguration();
			return false;
		}
	}

	private void startMainLogic() {
		
		var logic = new MainLogic(properties, cli);
		logic.execute();
	}
	
	@SneakyThrows
	private void printSampleConfiguration() {
		
		cli.println();
		cli.println("Define an application.yml in the JAR folder similar to this one:");
		
		cli.printSeparator();

		var sampleConfiguration = new ClassPathResource("application-sample.yml", this.getClass().getClassLoader());
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(sampleConfiguration.getInputStream()))) {
			
			String line = reader.readLine();
			while(line != null) {
					
				cli.println(line);
				line = reader.readLine();
			}
		}
		
		cli.printSeparator();
	}
}
