package com.utils.releaseshelper.model.logic;

import java.util.List;

import com.utils.releaseshelper.model.config.Config;

import lombok.Data;

/**
 * Main logic data, with all util data
 */
@Data
public class MainLogicData {

	private Config config;
	private ActionFlags actionFlags;
	private List<Procedure> procedures;
	private String optionalPreSelectedProcedureIndex;
}
