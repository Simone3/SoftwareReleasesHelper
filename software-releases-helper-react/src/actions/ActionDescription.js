import './ActionDescription.css';
import React from 'react';
import Utils from '../utils/Utils';

/**
 * The action description for the details section
 */
const ActionDescription = ({ action, variableValues }) => {

	const renderJenkinsBuildDescription = () => {
		return (
			<div className='action-description'>
				Jenkins build:
				<ul>
					<li>
						URL: {Utils.replacePlaceholders(action.url, variableValues)}
					</li>
					<li>
						Parameters: {
							action.parameters && action.parameters.length > 0 ?
							<ul>
								{action.parameters.map((parameter) => {
									return (
										<li key={parameter.key}>{parameter.key}: {Utils.replacePlaceholders(parameter.value, variableValues)}</li>
									);
								})}
							</ul> :
							'none'
						}
					</li>
				</ul>
			</div>
		);
	};

	const renderGitMergesDescription = () => {
		return (
			<div className='action-description'>
				Git merges:
				<ul>
					<li>
						Folder: {Utils.replacePlaceholders(action.repositoryFolder, variableValues)}
					</li>
					<li>
						Branches: {Utils.replacePlaceholders(action.merges, variableValues)}
					</li>
					<li>
						Pull: {action.pull ? 'yes' : 'no'}
					</li>
				</ul>
			</div>
		);
	};

	const renderGitPullAllDescription = () => {
		return (
			<div className='action-description'>
				Pull all Git repositories:
				<ul>
					<li>
						Parent folder: {Utils.replacePlaceholders(action.parentFolder, variableValues)}
					</li>
					<li>
						Skip if working tree is dirty: {action.skipIfWorkingTreeDirty ? 'yes' : 'no'}
					</li>
				</ul>
			</div>
		);
	};

	const renderOsCommandsDescription = () => {
		return (
			<div className='action-description'>
				Operating systems commands:
				<ul>
					<li>
						Folder: {Utils.replacePlaceholders(action.folder, variableValues)}
					</li>
					<li>
						Commands:
						<ul>
							{action.commands.map((command, index) => {
								return (
									<li key={index}>{Utils.replacePlaceholders(command.command, variableValues)}</li>
								);
							})}
						</ul>
					</li>
					<li>
						Git commit: {
							action.gitCommit ?
							<ul>
								<li>Branch: {Utils.replacePlaceholders(action.gitCommit.branch, variableValues)}</li>
								<li>Message: {Utils.replacePlaceholders(action.gitCommit.message, variableValues)}</li>
								<li>Pull: {action.gitCommit.pull ? 'yes' : 'no'}</li>
							</ul> :
							'none'
						}
					</li>
				</ul>
			</div>
		);
	};

	switch(action.type) {
		case 'JENKINS_BUILD':
			return renderJenkinsBuildDescription();
		case 'GIT_MERGES':
			return renderGitMergesDescription();
		case 'GIT_PULL_ALL':
			return renderGitPullAllDescription();
		case 'OPERATING_SYSTEM_COMMANDS':
			return renderOsCommandsDescription();
		default:
			console.error(`Unmapped action description for type: ${action.type}`);
			return null;
	}
};

export default ActionDescription;
