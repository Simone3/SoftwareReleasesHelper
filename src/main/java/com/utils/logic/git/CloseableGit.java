package com.utils.logic.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

class CloseableGit extends Git {

	CloseableGit(Repository repo) {
		
		super(repo);
	}

	@Override
	public void close() {
		
		super.close();
		getRepository().close();
	}
}
