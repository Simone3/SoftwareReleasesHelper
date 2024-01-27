package com.utils.releaseshelper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.utils.releaseshelper.model.domain.VariableDefinition;
import com.utils.releaseshelper.model.domain.VariableDefinitionType;
import com.utils.releaseshelper.model.misc.KeyValuePair;

class VariableUtilsGetValuesTest {

	@Test
	void testGetNull() {
		
		assertEquals(
			List.of(),	
			VariableUtils.getVariableValues(
				null,
				null
			)
		);
	}

	@Test
	void testGetNormal() {
		
		assertEquals(
			List.of(
				new KeyValuePair("var1", "Some Static Value"),
				new KeyValuePair("var2", "Some Other Value"),
				new KeyValuePair("var3", "Option C"),
				new KeyValuePair("var4", "Option E")
			),	
			VariableUtils.getVariableValues(
				List.of(
					staticVar("var1", "Some Static Value"),
					textVar("var2", false),
					freeSelectVar("var3", false, "Option A", "Option B", "Option C"),
					strictSelectVar("var4", "Option D", "Option E", "Option F")
				),
				List.of(
					new KeyValuePair("var2", "Some Other Value"),
					new KeyValuePair("var3", "Option C"),
					new KeyValuePair("var4", "Option E")
				)
			)
		);
	}

	@Test
	void testGetWhitespace() {
		
		assertEquals(
			List.of(
				new KeyValuePair("var1", "Some Static Value"),
				new KeyValuePair("var2", "SomeSpacedValue"),
				new KeyValuePair("var3", "SomeSpacedFreeText"),
				new KeyValuePair("var4", "Option E")
			),	
			VariableUtils.getVariableValues(
				List.of(
					staticVar("var1", "Some Static Value"),
					textVar("var2", true),
					freeSelectVar("var3", true, "OptionA", "OptionB", "OptionC"),
					strictSelectVar("var4", "Option D", "Option E", "Option F")
				),
				List.of(
					new KeyValuePair("var2", "    Some Spaced    Value"),
					new KeyValuePair("var3", "Some    Spaced Free Text    "),
					new KeyValuePair("var4", "Option E")
				)
			)
		);
	}

	@Test
	void testGetInvalidStrictSelect() {
		
		assertThrows(IllegalStateException.class, () -> {
			
			VariableUtils.getVariableValues(
				List.of(
					staticVar("var1", "Some Static Value"),
					textVar("var2", false),
					freeSelectVar("var3", false, "Option A", "Option B", "Option C"),
					strictSelectVar("var4", "Option D", "Option E", "Option F")
				),
				List.of(
					new KeyValuePair("var2", "Some Other Value"),
					new KeyValuePair("var3", "Option X"),
					new KeyValuePair("var4", "Option Y")
				)
			);
		});
	}
	
	private VariableDefinition staticVar(String key, String value) {
		
		VariableDefinition variable = new VariableDefinition();
		variable.setKey(key);
		variable.setType(VariableDefinitionType.STATIC);
		variable.setValue(value);
		return variable;
	}
	
	private VariableDefinition textVar(String key, boolean removeWhitespace) {
		
		VariableDefinition variable = new VariableDefinition();
		variable.setKey(key);
		variable.setType(VariableDefinitionType.TEXT);
		variable.setRemoveWhitespace(removeWhitespace);
		return variable;
	}
	
	private VariableDefinition freeSelectVar(String key, boolean removeWhitespace, String... options) {
		
		VariableDefinition variable = new VariableDefinition();
		variable.setKey(key);
		variable.setType(VariableDefinitionType.FREE_SELECT);
		variable.setOptions(List.of(options));
		variable.setRemoveWhitespace(removeWhitespace);
		return variable;
	}
	
	private VariableDefinition strictSelectVar(String key, String... options) {
		
		VariableDefinition variable = new VariableDefinition();
		variable.setKey(key);
		variable.setType(VariableDefinitionType.STRICT_SELECT);
		variable.setOptions(List.of(options));
		return variable;
	}
}
