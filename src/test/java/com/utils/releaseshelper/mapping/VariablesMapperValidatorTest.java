package com.utils.releaseshelper.mapping;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.validation.ValidationException;

class VariablesMapperValidatorTest {
	
	@Test
	void testVariableDefinitionEmpty() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition(" ");
		});
	}

	@Test
	void testVariableDefinitionStatic() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("sample");
		assertEquals(new ValueDefinition("sample", false, false), result);
	}

	@Test
	void testVariableDefinitionAskMe() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me}");
		assertEquals(new ValueDefinition(null, true, false), result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespace() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,remove-whitespace}");
		assertEquals(new ValueDefinition(null, true, true), result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue}");
		assertEquals(new ValueDefinition("myDefaultValue", true, false), result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,remove-whitespace}");
		assertEquals(new ValueDefinition("myDefaultValue", true, true), result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceMultipleColons() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue:with:extra:colons,remove-whitespace:useless-value}");
		assertEquals(new ValueDefinition("myDefaultValue:with:extra:colons", true, true), result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceTrim() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("  { ask-me , remove-whitespace , default : myDefaultValue }   ");
		assertEquals(new ValueDefinition("myDefaultValue", true, true), result);
	}

	@Test
	void testVariableDefinitionAskMeUnknown() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,unknownOption:something}");
		});
	}
}
