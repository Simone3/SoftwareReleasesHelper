package com.utils.releaseshelper.connector.jenkins;

import java.util.Map;

import com.utils.releaseshelper.model.error.MockException;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Jenkins connector, for test purposes
 */
@Slf4j
public class JenkinsConnectorMock implements JenkinsConnector {
	
	private int errorsToThrow = 0;
	
	private int thrownErrors = 0;

	@Override
	public String getCrumb(String crumbUrl, String username, String password) {
		
		log.warn("Jenkins invocations disabled: skipping get crumb command with {}, {}", crumbUrl, username);
		return "MOCK-CRUMB";
	}
	
	@Override
	public void startBuild(String buildUrl, String username, String password, String crumb, Map<String, String> parameters) {
		
		log.warn("Jenkins invocations disabled: skipping start build command with {}, {}, {}", buildUrl, username, parameters);
		
		if(errorsToThrow > 0) {
			
			if(thrownErrors >= errorsToThrow) {
				
				thrownErrors = 0;
			}
			else {
				
				thrownErrors++;
				throw new MockException("This is a mock Jenkins error!");
			}
		}
	}
}
