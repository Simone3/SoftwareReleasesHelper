package com.utils.releaseshelper.utils;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

/**
 * Helper util for URLs
 */
@UtilityClass
public class UrlUtils {

	public static String getFullUrl(String optionalBaseUrl, String url) {
		
		return StringUtils.isBlank(optionalBaseUrl) ? url : URI.create(optionalBaseUrl).resolve(url).toString();
	}
}
