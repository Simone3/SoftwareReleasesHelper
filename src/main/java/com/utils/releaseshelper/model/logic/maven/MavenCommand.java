package com.utils.releaseshelper.model.logic.maven;

import java.util.List;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.VariableDefinition;

import lombok.Data;

/**
 * Description of a Maven command
 */
@Data
public class MavenCommand {

	private ValueDefinition goals;
	private List<VariableDefinition> arguments;
	private boolean printMavenOutput;
}
