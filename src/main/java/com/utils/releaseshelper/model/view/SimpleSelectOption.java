package com.utils.releaseshelper.model.view;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A default implementation of SelectOption
 */
@Data
@RequiredArgsConstructor
public class SimpleSelectOption implements SelectOption {

	public final String optionName;
}
