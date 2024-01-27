package com.utils.releaseshelper.connector.git;

import java.io.Closeable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

/**
 * Helper class to wrap a Git repository
 */
public class GitRepository implements Closeable {

	private final Git git;
	
	GitRepository(Repository repo) {
		
		git = new Git(repo);
	}

	@Override
	public void close() {
		
		git.close();
		git.getRepository().close();
	}
	
	Git getHandler() {
		
		return git;
	}
}
