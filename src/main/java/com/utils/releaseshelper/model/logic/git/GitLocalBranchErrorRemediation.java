package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for check Git branches error
 */
@Getter
@RequiredArgsConstructor
public enum GitLocalBranchErrorRemediation implements ErrorRemediationOption {
	
	RETRY("Retry checking local branches", "Retrying branch check...", ErrorRemediation.RETRY),
	STOP("Interrupt the action", "Action interrupted for local branch error", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
