package com.utils.releaseshelper.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.utils.releaseshelper.model.error.ValidationException;
import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.ValueDefinitionType;

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
		assertValueDefinitionEquals("sample", ValueDefinitionType.STATIC, false, null, result);
	}

	@Test
	void testVariableDefinitionAskMe() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me}");
		assertValueDefinitionEquals(null, ValueDefinitionType.DYNAMIC_TEXT, false, null, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespace() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,remove-whitespace}");
		assertValueDefinitionEquals(null, ValueDefinitionType.DYNAMIC_TEXT, true, null, result);
	}

	@Test
	void testVariableDefinitionAskMeDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue}");
		assertValueDefinitionEquals("myDefaultValue", ValueDefinitionType.DYNAMIC_TEXT, false, null, result);
	}

	@Test
	void testVariableDefinitionAskMeEmptyDefault() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:}");
		});
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,remove-whitespace}");
		assertValueDefinitionEquals("myDefaultValue", ValueDefinitionType.DYNAMIC_TEXT, true, null, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceMultipleColons() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue:with:extra:colons,remove-whitespace:useless-value}");
		assertValueDefinitionEquals("myDefaultValue:with:extra:colons", ValueDefinitionType.DYNAMIC_TEXT, true, null, result);
	}

	@Test
	void testVariableDefinitionAskMeRemoveWhitespaceTrim() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("  { ask-me , remove-whitespace , default : myDefaultValue }   ");
		assertValueDefinitionEquals("myDefaultValue", ValueDefinitionType.DYNAMIC_TEXT, true, null, result);
	}

	@Test
	void testVariableDefinitionAskMeUnknown() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,default:myDefaultValue,unknownOption:something}");
		});
	}

	@Test
	void testVariableDefinitionAskMeOptions() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ ask-me, select-options: a | b | c | d }");
		assertValueDefinitionEquals(null, ValueDefinitionType.DYNAMIC_SELECT, false, List.of("a", "b", "c", "d"), result);
	}

	@Test
	void testVariableDefinitionAskMeEmptyOptions() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ask-me,select-options:}");
		});
	}

	@Test
	void testVariableDefinitionAskMeOptionsDefault() {
		
		ValueDefinition result = VariablesMapperValidator.mapAndValidateValueDefinition("{ ask-me, select-options: a | b | c | d, default: 2 }");
		assertValueDefinitionEquals("2", ValueDefinitionType.DYNAMIC_SELECT, false, List.of("a", "b", "c", "d"), result);
	}

	@Test
	void testVariableDefinitionAskMeOptionsWrongDefault() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ ask-me, select-options: a | b | c | d, default: c }");
		});
	}

	@Test
	void testVariableDefinitionAskMeOptionsOutOfRangeDefault() {
		
		assertThrows(ValidationException.class, () -> {
			
			VariablesMapperValidator.mapAndValidateValueDefinition("{ ask-me, select-options: a | b | c | d, default: 4 }");
		});
	}
	
	private void assertValueDefinitionEquals(String expectedStaticContent, ValueDefinitionType expectedDefinitionType, boolean expectedRemoveWhitespace, List<String> expectedOptions, ValueDefinition actual) {
		
		assertEquals(expectedStaticContent, actual.getStaticContent());
		assertEquals(expectedDefinitionType, actual.getDefinitionType());
		assertEquals(expectedRemoveWhitespace, actual.isRemoveWhitespace());
		
		if(actual.getOptions() != null) {
			
			assertEquals(expectedOptions, actual.getOptions().stream().map(option -> option.getOptionName()).collect(Collectors.toList()));
		}
		else {
			
			assertNull(expectedOptions);
		}
	}
}
