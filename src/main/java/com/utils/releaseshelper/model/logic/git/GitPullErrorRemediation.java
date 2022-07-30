package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for Git pull error
 */
@Getter
@RequiredArgsConstructor
public enum GitPullErrorRemediation implements ErrorRemediationOption {

	RETRY("Retry pulling", "Retrying pull...", ErrorRemediation.RETRY),
	SKIP("Skip pulling and continue the action", "Pull skipped", ErrorRemediation.SKIP),
	STOP("Interrupt the action", "Action interrupted for pull error", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
