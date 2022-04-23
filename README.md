
## Software Releases Helper


### Description

Simple command-line utility that allows to automate some actions for software releases.

Actions include Git merges, Jenkins builds and Maven commands. 

Actions are grouped by category and project, with multiple-selection and chaining options.




### Install and run

- [Download the JAR file](https://github.com/Simone3/SoftwareReleasesHelper/raw/main/downloads/SoftwareReleasesHelper.jar)
- Define your own categories/projects/actions (see [Configuration](#configuration))
- Run the JAR with `java -jar SoftwareReleasesHelper.jar`

Java 11 or above is required to run the util.




### Configuration

Configuration files define the custom categories, projects and actions to run.

This can be done with one or more standard [Spring Boot configuration files](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config).

The simplest way is to define an `application.yml` file in the JAR folder. As an alternative (or in addition to) the util also reads by default  `application-categories.yml`, `application-actions.yml` and `application-configuration.yml`, but this can be overridden by defining custom Spring profiles.

See [application-sample.yml](https://github.com/Simone3/SoftwareReleasesHelper/blob/main/src/main/resources/application-sample.yml) for a sample configuration file.


#### Dynamic Value Definitions

In the following sections, some fields can be specified with a dynamic value. They are strings that can have:
- a purely static value (a normal string), e.g. `'sample'`
- a value with `#[...]` placeholders that map previously defined [variables](#define-variables-action), e.g. `'Value with placeholder #[my-variable-key]'` (note: the escape character is another `#`)
- a run-time value `'{ask-me}'`, i.e. the user will be prompted for a value. Options for this type of value are available to further define the prompt behavior:
	- `remove-whitespace` (e.g. `'{ask-me,remove-whitespace}'`): the user input will be stripped of all white spaces


#### Actions

Actions are the building blocks of the application. They define a specific function, e.g. run a Jenkins build.

##### Define Variables Action
It allows to define one or more variables that can be used in following actions as placeholders (see [dynamic values](#dynamic-value-definitions)). Variables are defined with a project scope, i.e. are valid only for the list of actions of a single project.

Fields:
- `type`: `DEFINE_VARIABLES` (required)
- `name`: action name, it must be unique for all actions (required)
- `variables`: key-value map of variables to define (at least one required), values can be [dynamic](#dynamic-value-definitions) 

Example:
```
  -
    name: 'My Define Vars Action'
    type: 'DEFINE_VARIABLES'
    variables:
      my-first-var: 'my-fixed-value'
      my-second-var: '{ask-me}'
```

##### Jenkins Build Action
It allows to start a Jenkins build.

Fields:
- `type`: `JENKINS_BUILD` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `url`: the relative Jenkins URL (required)
- `parameters`: key-value map of build parameters (optional), values can be [dynamic](#dynamic-value-definitions) 

It requires a [Jenkins configuration](#jenkins-config).

Example:
```
  -
    name: 'My Jenkins Action'
    type: 'JENKINS_BUILD'
    url: '/my-build/url'
    parameters:
      my-first-param: 'sample'
      my-second-param: '{ask-me,remove-whitespace}'
      my-third-param: 'Variable replacement #[my-first-var]'
```

##### Git Merges Action
It allows to perform one or more Git merges.

Fields:
- `type`: `GIT_MERGES` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `repository-folder`: the absolute or relative path to the repository folder (required)
- `merges`: the list of merges (at least one required)
	- `source-branch`: the source branch, values can be [dynamic](#dynamic-value-definitions) 
	- `target-branch`: the target branch, value can be [dynamic](#dynamic-value-definitions) 
	- `pull`: if `true` the util will pull both source and target branches before the merge (default `false`)

It requires a [Git configuration](#git-config).

Example:
```
  -
    name: 'My Merges Action'
    type: 'GIT_MERGES'
    repository-folder: '/my-folder'
    merges:
      -
        source-branch: 'my-branch'
        target-branch: '{ask-me,remove-whitespace}'
        pull: true
      -
        source-branch: 'this-branch'
        target-branch: 'that-branch'
        pull: false
```

##### Maven Commands Action
It allows to run one or more Maven commands, optionally committing any resulting change to a Git repository.

Fields:
- `type`: `MAVEN_COMMANDS` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `project-folder`: the absolute or relative path to the project folder (required)
- `commands`: the list of commands to run (at least one required)
	- `goals`: the goal(s) of the command, separated by a space (required), value can be [dynamic](#dynamic-value-definitions)
	- `arguments`: key-value map of command arguments (optional), values can be [dynamic](#dynamic-value-definitions)
	- `offline`: if `true` the command is executed in offline mode (default `false`)
	- `print-maven-output`: if `true` the full Maven output will be printed (default `false`)
- `gitCommit`: definition of the Git commit for any resulting change (optional)
	- `branch`: the Git branch (required), value can be [dynamic](#dynamic-value-definitions)
	- `commit-message`: the commit message  (required), value can be [dynamic](#dynamic-value-definitions)
	- `pull`: if `true` the util will pull the branch before running the commands (default `false`)

It requires a [Maven configuration](#maven-config) and, if a Git commit is defined, a [Git configuration](#git-config).

Example:
```
  -
    name: 'My Maven Action'
    type: 'MAVEN_COMMANDS'
    project-folder: '/my-folder/project'
    commands:
      -
        goals: 'versions:set'
        arguments:
          newVersion: '#[my-second-var]'
          generateBackupPoms: 'false'
      -
        goals: 'clean install'
        print-maven-output: true
    git-commit:
      branch: 'my-branch'
      pull: true
      commit-message: 'My message with variable #[my-second-var]'
```

##### Chain Action
It allows to chain two or more actions. The advantage of doing this instead of simply placing the list of actions in a project is that a single confirmation prompt will be displayed.

Fields:
- `type`: `CHAIN` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `actions`: the list of action names to chain (at least one required)

Example:
```
  -
    name: 'My Chain Action'
    type: 'CHAIN'
    actions:
      - 'My Jenkins Action'
      - 'My Merges Action'
      - 'My Maven Action'
```


#### Projects

Projects are containers for actions. Once a project is selected, all its actions will be run in sequence.

Fields:
- `name`: the project name, it must be unique inside its category (required)
- `action-names`: the list of actions to run for this project (at least one required)

Example:
```
      -
        name: 'My First Project'
        action-names:
          - 'My Define Vars Action'
          - 'My Jenkins Action'
```


#### Categories

Categories are containers for projects. The user will be prompted to choose one category and then which of its projects to run (multiple selection).

Fields:
- `name`: the category name, it must be unique (required)
- `projects`: the list of projects for this category (at least one required)

Example:
```
  -
    name: 'My First Category'
    projects:
      -
        name: 'My First Project'
        action-names:
          - 'My Define Vars Action'
          - 'My Jenkins Action'
      -
        name: 'My Second Project'
        action-names:
          - 'My Define Vars Action'
          - 'My Merges Action'
          - 'My Maven Action'
```


#### Git Config

Global Git configuration. It is required if at least one action is Git-related.

Fields:
- `base-path`: base path for all Git repository paths (optional)
- `username`: the Git username (required)
- `password`: the Git password (required)
- `merge-message`: the merge message, it can optionally use the `#[SOURCE_BRANCH]` and `#[TARGET_BRANCH]` placeholders (required)
- `timeout-milliseconds`: the timeout in milliseconds for remote operations (e.g. pull) (required)

Example:
```
git:
  base-path: '~/Desktop/example'
  username: 'my-git-user'
  password: 'my-git-password'
  merge-message: 'Merge branch ''#[SOURCE_BRANCH]'' into ''#[TARGET_BRANCH]'' (auto-merge)'
  timeout-milliseconds: 5000
```


#### Jenkins Config

Global Jenkins configuration. It is required if at least one action is Jenkins-related.

Fields:
- `base-url`: the base URL for all Jenkins URLs (required)
- `crumb-url`: the relative URL of the Jenkins Crumb Issuer (required)
- `username`: the Jenkins username (required)
- `password`: the Jenkins password (required)
- `insecure-https`: if `true` certificate validation for HTTPS will be skipped (default `false`)
- `timeout-milliseconds`: the timeout in milliseconds for Jenkins invocations (required)

Example:
```
jenkins:
  base-url: 'http://myjenkins.com'
  crumb-url: '/crumbIssuer/api/json'
  username: 'my-jenkins-user'
  password: 'my-jenkins-password'
  insecure-https: false
  timeout-milliseconds: 5000
```


#### Maven Config

Global Maven configuration. It is required if at least one action is Maven-related.

Fields:
- `maven-home-folder`: the Maven Home folder (required)
- `base-path`: the base path for all Maven project folders (optional)

Example:
```
maven:
  maven-home-folder: '/Library/apache-maven-3.6.3'
  base-path: '~/Desktop/example'
```


#### Extra configuration

Fields:
- `test-mode`: if `true` all connectors are mocked and therefore no Jenkins, Git or Maven operations will be actually performed (default `false`)
- `print-passwords`: if `true` passwords will be displayed on screen (default `false`)
- `optional-pre-selected-category-index`: the user won't be prompted for category selection but instead taken directly to the specified index (optional)
- `optional-pre-selected-project-indices`: the user won't be prompted for projects selection but instead taken directly to the specified index(es) (optional)

Example:
```
test-mode: false
print-passwords: false
optional-pre-selected-category-index: '1'
optional-pre-selected-project-indices: '2,4,5'
```



### Tech documentation

The util is implemented as a Spring Boot application (Java 11, Maven).

The application **entry point** is *Runner.java*, a Spring *CommandLineRunner* that validates the properties and then runs the main logic.

**Mapping and validation** of input properties is performed by *PropertiesMapperValidator.java* and its child classes.

The **logic layer** contains all classes that orchestrate the business logic. In particular, *MainLogic.java* implements the category and project choices and then calls the *ActionDispatcher.java* to run each project actions. The Action Dispatcher in turn invokes the implementations of *ActionLogic.java* that execute the action logic.

The **service layer** is called by the logic layer for specific tasks. The *GitService.java* implements all Git business logic, the *JenkinsService.java* all the Jenkins business logic and *MavenService.java* all the Maven business logic.

The **connector layer** is called by the service layer and has the task of actually connecting to external components. The *GitConnector.java* allows to connect to a Git repository (via *JGit*), the *JenkinsConnector.java* allows to connect to a Jenkins server (via REST APIs) and *MavenConnector.java* allows to connect to a Maven project (via *Maven Invoker*).

Logic and service components all use the *CommandLineInterface.java* for **user input and output**.




