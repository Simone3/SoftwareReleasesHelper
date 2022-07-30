package com.utils.releaseshelper.model.logic.action;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for action error
 */
@Getter
@RequiredArgsConstructor
public enum ActionErrorRemediation implements ErrorRemediationOption {

	RETRY("Retry this action", "Retrying action...", ErrorRemediation.RETRY),
	SKIP("Skip this action and continue the step", "Action skipped", ErrorRemediation.SKIP),
	STOP("Interrupt the procedure", "Procedure interrupted for action error", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
