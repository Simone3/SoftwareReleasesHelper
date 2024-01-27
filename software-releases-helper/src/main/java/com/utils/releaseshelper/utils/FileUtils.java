package com.utils.releaseshelper.utils;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

/**
 * Helper util for files
 */
@UtilityClass
public class FileUtils {

	public static String getFullPath(String optionalBasePath, String path) {
		
		return StringUtils.isBlank(optionalBasePath) ? path : Paths.get(optionalBasePath, path).toString();
	}
}
