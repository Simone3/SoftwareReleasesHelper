package com.utils.releaseshelper.model.logic.maven;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for Maven command error
 */
@Getter
@RequiredArgsConstructor
public enum MavenCommandErrorRemediation implements ErrorRemediationOption {

	RETRY("Retry running the command", "Retrying Maven command...", ErrorRemediation.RETRY),
	SKIP("Skip this command and continue the action", "Maven command skipped", ErrorRemediation.SKIP),
	STOP("Interrupt the action", "Action interrupted for Maven command error", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
