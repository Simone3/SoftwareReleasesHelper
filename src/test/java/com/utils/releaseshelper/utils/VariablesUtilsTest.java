package com.utils.releaseshelper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.utils.releaseshelper.model.logic.ValueDefinition;
import com.utils.releaseshelper.model.logic.ValueDefinitionType;

class VariablesUtilsTest {

	@Test
	void testStaticValue() {
		
		ValueDefinition valueDefinition = new ValueDefinition(0, " a b c ", ValueDefinitionType.STATIC, null, false);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, null);
		assertEquals(" a b c ", value);
	}

	@Test
	void testStaticValueRemoveWhitespace() {
		
		ValueDefinition valueDefinition = new ValueDefinition(0, " a b c ", ValueDefinitionType.STATIC, null, true);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, null);
		assertEquals("abc", value);
	}

	@Test
	void testDynamicValue() {
		
		Map<String, String> sourceVariables = Map.of("myVar1", "thisVal1");
		ValueDefinition valueDefinition = new ValueDefinition(0, " a b c #[myVar1] ", ValueDefinitionType.STATIC, null, false);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, sourceVariables);
		assertEquals(" a b c thisVal1 ", value);
	}

	@Test
	void testDynamicValueReplaceWhitespace() {
		
		Map<String, String> sourceVariables = Map.of("myVar1", " this Val 1 ");
		ValueDefinition valueDefinition = new ValueDefinition(0, " a b c #[myVar1] ", ValueDefinitionType.STATIC, null, true);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, sourceVariables);
		assertEquals("abcthisVal1", value);
	}

	@Test
	void testDynamicValueMultipleVariables() {
		
		Map<String, String> sourceVariables = Map.of("myVar1", "thisVal1", "myVar2", "thisVal2");
		ValueDefinition valueDefinition = new ValueDefinition(0, " a b c #[myVar1] d #[myVar2] e #[myVar3] f ##[myVar1] g ", ValueDefinitionType.STATIC, null, false);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, sourceVariables);
		assertEquals(" a b c thisVal1 d thisVal2 e #[myVar3] f #[myVar1] g ", value);
	}

	@Test
	void testDynamicValueJenkinsUrl() {
		
		Map<String, String> sourceVariables = Map.of("environment", "development", "project-name", "my-project");
		ValueDefinition valueDefinition = new ValueDefinition(0, "/job/#[environment]/job/#[project-name]/buildWithParameters", ValueDefinitionType.STATIC, null, false);
		String value = VariablesUtils.defineValue(null, null, valueDefinition, sourceVariables);
		assertEquals("/job/development/job/my-project/buildWithParameters", value);
	}
}
