package com.utils.releaseshelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.utils.releaseshelper.logic.main.MainLogic;
import com.utils.releaseshelper.mapping.PropertiesMapperValidator;
import com.utils.releaseshelper.model.logic.MainLogicData;
import com.utils.releaseshelper.model.properties.Properties;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * The application entry point, a Spring CLI runner
 * It validates the input properties and then runs the main logic
 */
@Slf4j
@Component
public class Runner implements CommandLineRunner {
	
	@Autowired
	private Properties properties;
	
	private final CommandLineInterface cli;
	
	public Runner() {
		
		cli = new CommandLineInterface();
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		// Parse and validate properties
		MainLogicData mainLogicData;
		try {
			
			mainLogicData = PropertiesMapperValidator.mapAndValidateProperties(properties);
		}
		catch(Exception e) {

			log.error("Validation error", e);
			cli.printError("Invalid configuration: %s", e.getMessage());
			printSampleConfiguration();
			return;
		}
		
		// Run the main util logic
		MainLogic logic = new MainLogic(mainLogicData, cli);
		logic.execute();
	}
	
	@SneakyThrows
	private void printSampleConfiguration() {
		
		cli.println();
		cli.println("Define an application.yml in the JAR folder similar to this one:");
		
		cli.printSeparator();

		ClassPathResource sampleConfiguration = new ClassPathResource("application-sample.yml", this.getClass().getClassLoader());
		
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
