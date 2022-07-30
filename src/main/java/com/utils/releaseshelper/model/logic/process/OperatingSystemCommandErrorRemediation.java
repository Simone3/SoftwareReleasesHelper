package com.utils.releaseshelper.model.logic.process;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for OS command error
 */
@Getter
@RequiredArgsConstructor
public enum OperatingSystemCommandErrorRemediation implements ErrorRemediationOption {

	RETRY("Retry running the command", "Retrying OS command...", ErrorRemediation.RETRY),
	SKIP("Skip this command and continue the action", "OS command skipped", ErrorRemediation.SKIP),
	STOP("Interrupt the action", "Action interrupted for OS command error", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
