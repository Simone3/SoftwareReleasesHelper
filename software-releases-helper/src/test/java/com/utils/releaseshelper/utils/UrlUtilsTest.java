package com.utils.releaseshelper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UrlUtilsTest {

	@Test
	void testSimpleUrl() {
		
		String url = UrlUtils.getFullUrl(null, "http://test.com");
		assertEquals("http://test.com", url);
	}

	@Test
	void testSimpleUrlTrim() {
		
		String url = UrlUtils.getFullUrl(null, "    http://test.com ");
		assertEquals("http://test.com", url);
	}

	@Test
	void testBaseUrlNoSlash() {
		
		String url = UrlUtils.getFullUrl("http://start/here", "and/end/here");
		assertEquals("http://start/here/and/end/here", url);
	}

	@Test
	void testBaseUrlLeftSlash() {
		
		String url = UrlUtils.getFullUrl("http://start/here/", "and/end/here");
		assertEquals("http://start/here/and/end/here", url);
	}

	@Test
	void testBaseUrlRightSlash() {
		
		String url = UrlUtils.getFullUrl("http://start/here", "/and/end/here");
		assertEquals("http://start/here/and/end/here", url);
	}

	@Test
	void testBaseUrlBothSlashes() {
		
		String url = UrlUtils.getFullUrl("http://start/here/", "/and/end/here");
		assertEquals("http://start/here/and/end/here", url);
	}
	
	@Test
	void testEmpty() {
		
		assertThrows(IllegalStateException.class, () -> {
			
			UrlUtils.getFullUrl("http://start/here/", "    ");
		});
	}
}
