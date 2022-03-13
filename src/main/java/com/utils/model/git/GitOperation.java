package com.utils.model.git;

import lombok.Data;

@Data
public class GitOperation {

	private OperationType type;
	private boolean pull;
	private String sourceBranch;
	private String targetBranch;
	
	@Override
	public String toString() {
		
		switch(type) {
			
			case MERGE:
				return "Merge from " + sourceBranch + " to " + targetBranch + " (" + (pull ? "pulling from both branches" : "without pulling") + ")";
				
			default:
				throw new IllegalStateException("Unmapped type: " + type);
		}
	}
}
