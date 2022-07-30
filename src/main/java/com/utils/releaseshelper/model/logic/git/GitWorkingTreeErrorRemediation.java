package com.utils.releaseshelper.model.logic.git;

import com.utils.releaseshelper.model.error.ErrorRemediation;
import com.utils.releaseshelper.model.error.ErrorRemediationOption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Remediation options for Git working tree error
 */
@Getter
@RequiredArgsConstructor
public enum GitWorkingTreeErrorRemediation implements ErrorRemediationOption {

	RETRY("Retry checking working tree", "Retrying checking working tree...", ErrorRemediation.RETRY),
	STOP("Interrupt the action", "Action interrupted because of unclean working tree", ErrorRemediation.STOP);
	
	private final String optionName;
	private final String message;
	private final ErrorRemediation remediation;
}
