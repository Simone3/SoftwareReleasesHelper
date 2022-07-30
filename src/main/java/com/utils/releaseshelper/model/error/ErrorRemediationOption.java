package com.utils.releaseshelper.model.error;

import com.utils.releaseshelper.model.view.SelectOption;

/**
 * A select option that defines an error remediation
 */
public interface ErrorRemediationOption extends SelectOption {
	
	ErrorRemediation getRemediation();
	
	String getMessage();
}