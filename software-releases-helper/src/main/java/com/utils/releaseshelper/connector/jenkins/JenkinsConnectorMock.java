package com.utils.releaseshelper.connector.jenkins;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.util.LinkedMultiValueMap;

import com.utils.releaseshelper.model.error.MockException;
import com.utils.releaseshelper.model.logic.JenkinsCrumbData;
import com.utils.releaseshelper.model.misc.KeyValuePair;

import lombok.extern.slf4j.Slf4j;

/**
 * A mocked implementation of the Jenkins connector, for test purposes
 */
@Slf4j
public class JenkinsConnectorMock implements JenkinsConnector {
	
	private int errorProbability = 0;
	
	private long delay = 1000l;

	@Override
	public JenkinsCrumbData getCrumb(String crumbUrl, String username, String password) {
		
		log.warn("Jenkins invocations disabled: skipping get crumb command with {}, {}", crumbUrl, username);
		return new JenkinsCrumbData("MOCK-CRUMB", new LinkedMultiValueMap<>());
	}
	
	@Override
	public void startBuild(String buildUrl, String username, String password, JenkinsCrumbData crumb, List<KeyValuePair> parameters) {
		
		log.warn("Jenkins invocations disabled: skipping start build command with {}, {}, {}", buildUrl, username, parameters);
		
		if(delay > 0) {
			
			try {
				
				Thread.sleep(delay);
			}
			catch(InterruptedException e) {
				
				Thread.currentThread().interrupt();
			}
		}
		
		if(errorProbability > 0 && ThreadLocalRandom.current().nextInt(1, 101) <= errorProbability) {
			
			throw new MockException("This is a mock Jenkins error!");
		}
	}
}
