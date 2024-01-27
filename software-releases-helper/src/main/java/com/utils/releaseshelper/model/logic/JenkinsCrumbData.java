package com.utils.releaseshelper.model.logic;

import org.springframework.util.MultiValueMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Data for Jenkins crumbs
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JenkinsCrumbData {
	
	private String crumb;
	private MultiValueMap<String, String> cookies;
}
