package com.utils.releaseshelper.logic.main;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.utils.releaseshelper.logic.common.StateLogic;
import com.utils.releaseshelper.logic.procedure.ProcedureLogic;
import com.utils.releaseshelper.model.config.Config;
import com.utils.releaseshelper.model.config.GitConfig;
import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.config.MavenConfig;
import com.utils.releaseshelper.model.logic.ActionFlags;
import com.utils.releaseshelper.model.logic.MainLogicData;
import com.utils.releaseshelper.model.logic.MainState;
import com.utils.releaseshelper.model.logic.Procedure;
import com.utils.releaseshelper.service.main.MainService;
import com.utils.releaseshelper.view.CommandLineInterface;

import lombok.extern.slf4j.Slf4j;

/**
 * The main Logic class
 * It runs the main CLI program:
 * - pick procedures state
 * - run procedure state
 */
@Slf4j
public class MainLogic extends StateLogic<MainState> {

	private final MainLogicData mainLogicData;
	private final CommandLineInterface cli;
	private final MainService mainService;
	
	private Procedure procedure;
	
	public MainLogic(MainLogicData mainLogicData, CommandLineInterface cli) {
		
		super(MainState.PICK_PROCEDURE, MainState.EXIT);
		
		Config config = mainLogicData.getConfig();
		ActionFlags actionFlags = mainLogicData.getActionFlags();

		this.mainLogicData = mainLogicData;
		this.cli = cli;
		this.mainService = new MainService(config, actionFlags, cli);
	}

	public void execute() {
		
		try {
		
			printConfig();
			loopStates();
		}
		catch(Exception e) {

			log.error("Global exception in main logic", e);
			cli.printError("UNEXPECTED GLOBAL ERROR, APPLICATION WILL EXIT: %s", e.getMessage());
		}
	}
	
	private void printConfig() {
		
		Config config = mainLogicData.getConfig();
		boolean testMode = config.isTestMode();
		boolean printPasswords = config.isPrintPasswords();
		GitConfig git = config.getGit();
		JenkinsConfig jenkins = config.getJenkins();
		MavenConfig maven = config.getMaven();
		
		if(git != null) {

			String password = StringUtils.defaultIfBlank(git.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.println("Loaded Git config:");
			cli.println("  - Username: %s", StringUtils.defaultIfBlank(git.getUsername(), ""));
			cli.println("  - Password: %s", password);
			cli.println("  - Merge message: %s", StringUtils.defaultIfBlank(git.getMergeMessage(), ""));
			cli.println("  - Timeout (ms): %s", git.getTimeoutMilliseconds());
			cli.println();
		}
		
		if(jenkins != null) {

			String password = StringUtils.defaultIfBlank(jenkins.getPassword(), "");
			if(!password.isEmpty() && !printPasswords) {
				
				password = "********";
			}
			
			cli.println("Loaded Jenkins config:");
			cli.println("  - Crumb URL: %s", StringUtils.defaultIfBlank(jenkins.getCrumbUrl(), ""));
			cli.println("  - Username: %s", StringUtils.defaultIfBlank(jenkins.getUsername(), ""));
			cli.println("  - Password: %s", password);
			cli.println("  - Use crumb: %s", jenkins.isUseCrumb());
			cli.println("  - Insecure HTTPS: %s", jenkins.isInsecureHttps());
			cli.println("  - Timeout (ms): %s", jenkins.getTimeoutMilliseconds());
			cli.println();
		}
		
		if(maven != null) {

			cli.println("Loaded Maven config:");
			cli.println("  - Maven home: %s", StringUtils.defaultIfBlank(maven.getMavenHomeFolder(), ""));
			cli.println();
		}
		
		if(testMode) {
			
			cli.println("*****************************************************************");
			cli.println("*** WARNING: test mode is active, all actions will do nothing ***");
			cli.println("*****************************************************************");
			cli.println();
		}
	}
	
	@Override
	protected MainState processCurrentState(MainState currentState) {
		
		switch(currentState) {
		
			case PICK_PROCEDURE:
				return pickProcedure();
		
			case RUN_PROCEDURE:
				return runProcedure();
				
			default:
				throw new IllegalStateException("Unrecognized state: " + currentState);
		}
	}
	
	private MainState pickProcedure() {
		
		List<Procedure> allProcedures = mainLogicData.getProcedures();
		String optionalPreSelection = mainLogicData.getOptionalPreSelectedProcedureIndex();
		
		procedure = cli.askUserSelection("Procedures", allProcedures, optionalPreSelection, !StringUtils.isBlank(optionalPreSelection));
		
		return MainState.RUN_PROCEDURE;
	}
	
	private MainState runProcedure() {
		
		// Run the procedure
		ProcedureLogic procedureLogic = new ProcedureLogic(procedure, mainService, cli);
		procedureLogic.run();

		// Clear the state for the next run
		clearState();
		
		// Ask confirmation before restarting if the user will not manually pick the procedure
		if(mainLogicData.getProcedures().size() == 1 || !StringUtils.isBlank(mainLogicData.getOptionalPreSelectedProcedureIndex())) {
			
			if(cli.askUserConfirmation("Restart from the beginning")) {

				return MainState.PICK_PROCEDURE;
			}
			else {

				return MainState.EXIT;
			}
		}
		else {
			
			return MainState.PICK_PROCEDURE;
		}
	}

	private void clearState() {
		
		this.procedure = null;
	}
}
