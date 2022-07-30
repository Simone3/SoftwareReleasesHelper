package com.utils.releaseshelper.connector.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.util.CollectionUtils;

import com.utils.releaseshelper.model.config.MavenConfig;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.view.output.CommandLineOutputHandler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of the Maven connector based on the Apache Maven Invoker library
 */
@Slf4j
public class MavenConnectorReal implements MavenConnector {
	
	private final File mavenHome;

	public MavenConnectorReal(MavenConfig mavenConfig) {
		
		mavenHome = getMavenHome(mavenConfig.getMavenHomeFolder());
	}

	@Override
	public File getPomFile(String projectFolder) {
		
		File pomFile = new File(Paths.get(projectFolder, "pom.xml").toString());
		
		if(!pomFile.exists()) {
			
			throw new BusinessException("pom.xml file " + pomFile.getAbsolutePath() + " does not exist!");
		}
		
		if(pomFile.isDirectory()) {
			
			throw new BusinessException("pom.xml file " + pomFile.getAbsolutePath() + " is a folder!");
		}
		
		return pomFile;
	}
	
	@Override
	public void runCommand(File pomFile, CommandLineOutputHandler outputHandler, String goals, Map<String, String> arguments, boolean offline) {
		
		Invoker invoker = getInvoker(outputHandler);
		
		List<String> goalsList = Arrays.asList(goals.split("\\s+"));
		
		Properties argumentProperties = new Properties();
		if(!CollectionUtils.isEmpty(arguments)) {
			
			argumentProperties.putAll(arguments);
		}
		
	    InvocationResult result = invoke(invoker, pomFile, goalsList, argumentProperties, offline);
	    validateResult(result);
	}

	private File getMavenHome(String mavenFolderPath) {
		
		File mavenFolder = new File(mavenFolderPath);
		
		if(!mavenFolder.exists()) {
			
			throw new BusinessException("Maven home " + mavenFolder.getAbsolutePath() + " does not exist!");
		}
		
		if(!mavenFolder.isDirectory()) {
			
			throw new BusinessException("Maven home " + mavenFolder.getAbsolutePath() + " is not a folder!");
		}
		
		return mavenFolder;
	}
	
	private Invoker getInvoker(CommandLineOutputHandler outputHandler) {
		
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(mavenHome);
		
		InvocationOutputHandler invocationOutputHandler = new CustomInvocationOutputHandler(outputHandler);
		invoker.setOutputHandler(invocationOutputHandler);
		invoker.setErrorHandler(invocationOutputHandler);
		
		return invoker;
	}
	
	@SneakyThrows
	private InvocationResult invoke(Invoker invoker, File pomFile, List<String> goals, Properties arguments, boolean offline) {
		
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(pomFile);
		request.setBatchMode(true);
		request.setGoals(goals);
		request.setProperties(arguments);
		request.setOffline(offline);
		return invoker.execute(request);
	}
	
	private void validateResult(InvocationResult result) {
		
		if(result.getExitCode() != 0) {
			
			if(result.getExecutionException() == null) {
				
				log.error("Maven invocation error without exception");
				throw new BusinessException("Maven Error " + result.getExitCode());
			}
			else {
				
				log.error("Maven invocation error", result.getExecutionException());
				throw new BusinessException("Maven Error " + result.getExitCode() + ": " + result.getExecutionException().getMessage());
			}
		}
	}
}
