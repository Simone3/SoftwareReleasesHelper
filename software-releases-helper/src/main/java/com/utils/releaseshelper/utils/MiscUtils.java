package com.utils.releaseshelper.utils;

import lombok.experimental.UtilityClass;

/**
 * Misc util
 */
@UtilityClass
public class MiscUtils {

	public static String formatMessage(String message, Object... args) {
	
		if(args == null || args.length == 0) {
			
			return message;
		}
		else {
			
			return String.format(message, args);
		}
	}
}
