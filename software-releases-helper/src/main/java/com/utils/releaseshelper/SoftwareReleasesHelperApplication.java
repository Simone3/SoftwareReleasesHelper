package com.utils.releaseshelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.utils.releaseshelper.model.properties.Properties;

/**
 * The Spring Boot application
 */
@SpringBootApplication
@EnableConfigurationProperties(Properties.class)
public class SoftwareReleasesHelperApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(SoftwareReleasesHelperApplication.class, args);
	}
}
