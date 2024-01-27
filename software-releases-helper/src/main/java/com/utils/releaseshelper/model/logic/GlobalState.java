package com.utils.releaseshelper.model.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The main global state for the application, i.e. state shared by all users
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class GlobalState {

	// Simple concurrent maps for now (they may be evolved with "real" caches to handle expiration time, e.g. suspension states stay there indefinitely if the user closes the browser window...)
	private final Map<String, ActionState> suspensionStates = new ConcurrentHashMap<>();
	private final Map<String, JenkinsCrumbData> jenkinsCrumbs = new ConcurrentHashMap<>();
	
	public ActionState getSuspensionState(String key) {
		
		return suspensionStates.get(key);
	}
	
	public ActionState removeSuspensionState(String key) {
		
		return suspensionStates.remove(key);
	}
	
	public void putSuspensionState(String key, ActionState state) {
		
		suspensionStates.put(key, state);
	}
	
	public JenkinsCrumbData getJenkinsCrumb(String key) {
		
		return jenkinsCrumbs.get(key);
	}
	
	public void putJenkinsCrumb(String key, JenkinsCrumbData state) {
		
		jenkinsCrumbs.put(key, state);
	}
}
