package com.utils.releaseshelper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.utils.releaseshelper.model.misc.KeyValuePair;

class VariableUtilsPlaceholderTest {

	@Test
	void testReplaceNull() {
		
		assertEquals(
			null,	
			VariableUtils.replacePlaceholders(
				null,
				List.of(
					new KeyValuePair("a", "b")
				)
			)
		);
	}

	@Test
	void testReplaceBlank() {
		
		assertEquals(
			"    ",	
			VariableUtils.replacePlaceholders(
				"    ",
				List.of(
					new KeyValuePair("a", "b")
				)
			)
		);
	}

	@Test
	void testReplaceNullPlaceholders() {
		
		assertEquals(
			"some #[variable] placeholder",	
			VariableUtils.replacePlaceholders(
				"some #[variable] placeholder",
				null
			)
		);
	}

	@Test
	void testReplaceNoPlaceholders() {
		
		assertEquals(
			"some #[variable] placeholder",	
			VariableUtils.replacePlaceholders(
				"some #[variable] placeholder",
				List.of()
			)
		);
	}

	@Test
	void testReplaceSimple() {
		
		assertEquals(
			"some value placeholder",	
			VariableUtils.replacePlaceholders(
				"some #[variable] placeholder",
				List.of(
					new KeyValuePair("variable", "value")
				)
			)
		);
	}

	@Test
	void testReplaceMultiple() {
		
		assertEquals(
			"some value placeholder value one value two",
			VariableUtils.replacePlaceholders(
				"some #[variable] placeholder #[variable] one #[variable] two",
				List.of(
					new KeyValuePair("variable", "value")
				)
			)
		);
	}

	@Test
	void testReplaceComplex() {
		
		assertEquals(
			"d some value1 placeholder something one value1 two def",
			VariableUtils.replacePlaceholders(
				"#[a] some #[variable1] placeholder #[variable2] one #[variable1] two #[a]#[b]#[c]",
				List.of(
					new KeyValuePair("variable1", "value1"),
					new KeyValuePair("variable2", "something"),
					new KeyValuePair("a", "d"),
					new KeyValuePair("b", "e"),
					new KeyValuePair("c", "f")
				)
			)
		);
	}

	@Test
	void testReplaceWrongPlaceholders() {
		
		assertEquals(
			"d some value1 placeholder #[variable3] one #[vari able1] two d#[b]f",
			VariableUtils.replacePlaceholders(
				"#[a] some #[variable1] placeholder #[variable3] one #[vari able1] two #[a]#[b]#[c]",
				List.of(
					new KeyValuePair("variable1", "value1"),
					new KeyValuePair("variable2", "something"),
					new KeyValuePair("a", "d"),
					new KeyValuePair("x", "y"),
					new KeyValuePair("c", "f")
				)
			)
		);
	}

	@Test
	void testReplaceMalformedPlaceholders() {
		
		assertEquals(
			"d some #[variable1 placeholder #[[variable2] one #{variable1] two def",
			VariableUtils.replacePlaceholders(
				"#[a] some #[variable1 placeholder #[[variable2] one #{variable1] two #[a]#[b]#[c]",
				List.of(
					new KeyValuePair("variable1", "value1"),
					new KeyValuePair("variable2", "something"),
					new KeyValuePair("a", "d"),
					new KeyValuePair("b", "e"),
					new KeyValuePair("c", "f")
				)
			)
		);
	}
}
