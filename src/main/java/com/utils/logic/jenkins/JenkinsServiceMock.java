package com.utils.logic.jenkins;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class JenkinsServiceMock implements JenkinsService {
	
	private static final int ERRORS = 1;
	private int errorsCounter = 0;

	@Override
	public String getCrumb(String crumbUrl, String username, String password) {
		
		log.warn("Jenkins invocations disabled: skipping get crumb command with {}, {}", crumbUrl, username);
		return "MOCK-CRUMB";
	}
	
	@Override
	public void startBuild(String buildUrl, String username, String password, String crumb, Map<String, String> parameters) {
		
		log.warn("Jenkins invocations disabled: skipping start build command with {}, {}, {}", buildUrl, username, parameters);
		
		if(ERRORS > 0) {
			
			if(errorsCounter >= ERRORS) {
				
				errorsCounter = 0;
			}
			else {
				
				errorsCounter++;
				throw new IllegalStateException("This is a mock Jenkins error!");
			}
		}
	}
}
