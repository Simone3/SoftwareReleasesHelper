package com.utils.releaseshelper.utils;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

/**
 * Helper util for URLs
 */
@UtilityClass
public class UrlUtils {

	public static String getFullUrl(String optionalBaseUrl, String url) {
		
		if(StringUtils.isBlank(url)) {
			
			throw new IllegalStateException("Invalid empty URL");
		}
		
		url = url.trim();
		
		if(StringUtils.isBlank(optionalBaseUrl)) {
			
			return url;
		}
		
		optionalBaseUrl = optionalBaseUrl.trim();
		
		if(!optionalBaseUrl.endsWith("/")) {
			
			optionalBaseUrl += "/";
		}
		
		if(url.startsWith("/")) {
			
			url = url.substring(1, url.length());
		}
		
		return optionalBaseUrl.concat(url);
	}
}
