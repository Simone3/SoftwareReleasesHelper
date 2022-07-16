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

	
	
    public static final String ANSI_RESET = "\u001B[0m";
  
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

	@Override
	public void run(String... args) throws Exception {
		
		
		System.out.println(ANSI_BLACK + "Black" + ANSI_RESET);
		System.out.println(ANSI_RED + "Red" + ANSI_RESET);
		System.out.println(ANSI_GREEN + "Green" + ANSI_RESET);
		System.out.println(ANSI_WHITE + "White" + ANSI_RESET);
		
		System.out.println(ANSI_BLACK_BACKGROUND + "Black" + ANSI_RESET);
		System.out.println(ANSI_RED_BACKGROUND + "Red" + ANSI_RESET);
		System.out.println(ANSI_GREEN_BACKGROUND + "Green" + ANSI_RESET);
		System.out.println(ANSI_WHITE_BACKGROUND + "White" + ANSI_RESET);
		
		System.out.println(ANSI_RED_BACKGROUND + ANSI_GREEN + "Asdasdasd!" + ANSI_RESET + ANSI_RESET);
		
		

		// Parse and validate properties
		MainLogicData mainLogicData;
		try {
			
			mainLogicData = PropertiesMapperValidator.mapAndValidateProperties(properties);
		}
		catch(Exception e) {
			
			cli.printError("Invalid configuration: %s", e.getMessage());
			log.error("Validation error", e);
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
