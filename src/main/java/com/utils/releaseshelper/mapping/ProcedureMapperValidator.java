package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.logic.Procedure;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.logic.step.Step;
import com.utils.releaseshelper.model.properties.ProcedureProperty;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates procedure properties
 */
@UtilityClass
public class ProcedureMapperValidator {

	public static List<Procedure> mapAndValidateProcedures(List<ProcedureProperty> proceduresProperties, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notEmpty(proceduresProperties, "At least one procedure should be defined");
		
		List<Procedure> procedures = new ArrayList<>();
		Set<String> procedureNames = new HashSet<>();
		
		for(int i = 0; i < proceduresProperties.size(); i++) {

			ProcedureProperty procedureProperty = proceduresProperties.get(i);
			
			Procedure procedure;
			try {
				
				procedure = mapAndValidateProcedure(procedureProperty, actionDefinitions);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid procedure at index " + i + " -> " + e.getMessage(), e);
			}

			String procedureName = procedureProperty.getName();
			if(procedureNames.contains(procedureName)) {

				throw new ValidationException("Procedure at index " + i + " has the same name of a previous procedure");
			}
			
			procedureNames.add(procedureName);
			procedures.add(procedure);
		}
		
		return procedures;
	}
	
	public static Procedure mapAndValidateProcedure(ProcedureProperty procedureProperty, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notNull(procedureProperty, "Procedure is empty");
		
		String name = ValidationUtils.notBlank(procedureProperty.getName(), "Procedure does not have a name");
		
		List<Step> steps;
		try {
			
			steps = StepMapperValidator.mapAndValidateSteps(procedureProperty.getSteps(), actionDefinitions);
		}
		catch(Exception e) {
			
			throw new ValidationException("Invalid steps for procedure -> " + e.getMessage(), e);
		}
		
		Procedure procedure = new Procedure();
		procedure.setName(name);
		procedure.setSteps(steps);
		return procedure;
	}
}
