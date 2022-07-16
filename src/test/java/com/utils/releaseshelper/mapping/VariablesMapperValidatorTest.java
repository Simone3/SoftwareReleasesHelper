package com.utils.releaseshelper.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertValueDefinitionEquals("sample", false, false, result);
	}

	@Test
	void testVariableDefinitionAskMe() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me}");
		assertValueDefinitionEquals(null, true, false, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespace() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,remove-whitespace}");
		assertValueDefinitionEquals(null, true, true, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue}");
		assertValueDefinitionEquals("myDefaultValue", true, false, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,remove-whitespace}");
		assertValueDefinitionEquals("myDefaultValue", true, true, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceMultipleColons() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue:with:extra:colons,remove-whitespace:useless-value}");
		assertValueDefinitionEquals("myDefaultValue:with:extra:colons", true, true, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceTrim() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("  { ask-me , remove-whitespace , default : myDefaultValue }   ");
		assertValueDefinitionEquals("myDefaultValue", true, true, result);
	}

	@Test
	void testVariableDefinitionAskMeUnknown() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,unknownOption:something}");
		});
	}
	
	private void assertValueDefinitionEquals(String expectedStaticContent, boolean expectedAskMe, boolean expectedRemoveWhitespace, ValueDefinition actual) {
		
		assertEquals(expectedStaticContent, actual.getStaticContent());
		assertEquals(expectedAskMe, actual.isAskMe());
		assertEquals(expectedRemoveWhitespace, actual.isRemoveWhitespace());
	}
}
