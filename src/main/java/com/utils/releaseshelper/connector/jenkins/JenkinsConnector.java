package com.utils.releaseshelper.connector.jenkins;

import java.util.Map;

import org.springframework.util.MultiValueMap;

import com.utils.releaseshelper.connector.Connector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The connector to interact with a Jenkins server
 */
public interface JenkinsConnector extends Connector {

	CrumbData getCrumb(String crumbUrl, String username, String password);
	
	void startBuild(String buildUrl, String username, String password, CrumbData crumbData, Map<String, String> parameters);
	
	// TODO move to model
	@Data
	@RequiredArgsConstructor
	@AllArgsConstructor
	public class CrumbData {
		
		private String crumb;
		private MultiValueMap<String, String> cookies;
	}
}
