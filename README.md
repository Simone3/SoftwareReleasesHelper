

## Software Releases Helper


### Description

Simple command-line utility that allows to automate some actions for software releases.

Actions include Git merges, Jenkins builds and Maven commands. 

Actions are chained in pre-defined procedures, that may loop to run some actions for one or more user-selected projects.


### Install and run

- [Download the JAR file](https://github.com/Simone3/SoftwareReleasesHelper/raw/main/downloads/SoftwareReleasesHelper.jar)
- Define your own procedures/actions (see [Configuration](#configuration))
- Run the JAR with `java -jar SoftwareReleasesHelper.jar`

Java 11 or above is required to run the util.


### Configuration

Configuration files define the custom procedures and actions to run.

This can be done with one or more standard [Spring Boot configuration files](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config).

The simplest way is to define an `application.yml` file in the same folder where the downloaded JAR is placed. As an alternative (or in addition to) the program also reads by default  `application-procedures.yml`, `application-actions.yml` and `application-configuration.yml`, but this can be overridden by defining custom Spring profiles.

See [application-sample.yml](https://github.com/Simone3/SoftwareReleasesHelper/blob/main/src/main/resources/application-sample.yml) for a sample configuration file.


#### Dynamic Value Definitions

In the following sections, some fields will be defined as "value can be dynamic". They are strings that can have:
- a purely static value (a normal string), e.g. `'sample'`.
- a value with `#[...]` placeholders that map previously defined [variables](#define-variables-action), e.g. `'Value with placeholder #[my-variable-key]'` (note: the escape character is another `#`).
- a run-time value `'{ ask-me }'`, i.e. the user will be prompted for a value. One or more specifiers may be defined to further customize the prompt behavior:
	- `remove-whitespace` (e.g. `'{ ask-me, remove-whitespace }'`): the user input will be stripped of all white spaces.
	- `default` (e.g. `'{ ask-me, default: myDefaultValue }'`): if the user submits a whitespace-only answer, the default value will be used.
	- `select-options` (e.g. `'{ ask-me, select-options: myOption1 | myOption2 }'`): the user will be prompted with a pre-defined selection instead of a free text input. Note: if `select-options` and `default` specifiers are both defined, the `default` value must be the index of the default option (starting with 0).


#### Actions

Actions are the building blocks of the application. They define a specific function, e.g. run a Jenkins build.

##### Define Variables Action
It allows to define one or more variables that can be used in following actions as placeholders (see [dynamic values](#dynamic-value-definitions)). Variables are defined with a procedure scope, i.e. are valid only for the list of actions of a single procedure. If variables are defined inside a project loop, they are valid only for that iteration of the loop.

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
      my-second-var: '{ ask-me, remove-whitespace }'
```

##### Jenkins Build Action
It allows to start a Jenkins build.

Fields:
- `type`: `JENKINS_BUILD` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `custom-description`: a custom description of the action (optional), if empty a default description will be used
- `url`: the relative Jenkins URL (required), value can be [dynamic](#dynamic-value-definitions)
- `parameters`: key-value map of build parameters (optional), values can be [dynamic](#dynamic-value-definitions) 

It requires a [Jenkins configuration](#jenkins-config).

Example:
```
  -
    name: 'My Jenkins Action'
    type: 'JENKINS_BUILD'
    url: '/job/my-build/job/#[project-name]/buildWithParameters'
    parameters:
      my-first-param: 'sample'
      my-second-param: '{ ask-me, remove-whitespace }'
      my-third-param: 'value with variable replacement #[my-first-var]'
```

##### Git Merges Action
It allows to perform one or more Git merges.

Fields:
- `type`: `GIT_MERGES` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `custom-description`: a custom description of the action (optional), value can be [dynamic](#dynamic-value-definitions), if empty a default description will be used
- `repository-folder`: the absolute or relative path to the repository folder (required), value can be [dynamic](#dynamic-value-definitions)
- `merges`: the merges to perform, defined by two or more branches separated by `->`, with multiple merge steps separated by `;` (e.g. `branch1 -> branch2 -> branch3; branch4 -> branch5` means that `branch1` will be merged into `branch2`, then `branch2` into `branch3`, then `branch4` into `branch5`), values can be [dynamic](#dynamic-value-definitions) 
- `pull`: if `true` the util will pull all source and target branches before all merges (default `false`)

It requires a [Git configuration](#git-config).

Example:
```
  -
    name: 'My Merges Action'
    type: 'GIT_MERGES'
    repository-folder: '/my-folder'
    merges: '{ ask-me, select-options: branch1 -> branch2 | branch3 -> branch4 -> branch5 }'
    pull: true
```

##### Maven Commands Action
It allows to run one or more Maven commands, optionally committing any resulting change to a Git repository.

Fields:
- `type`: `MAVEN_COMMANDS` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `custom-description`: a custom description of the action (optional), value can be [dynamic](#dynamic-value-definitions), if empty a default description will be used
- `project-folder`: the absolute or relative path to the project folder (required), value can be [dynamic](#dynamic-value-definitions)
- `commands`: the list of commands to run (at least one required)
	- `goals`: the goal(s) of the command, separated by a space (required), value can be [dynamic](#dynamic-value-definitions)
	- `arguments`: key-value map of command arguments (optional), values can be [dynamic](#dynamic-value-definitions)
	- `offline`: if `true` the command is executed in offline mode (default `false`)
	- `suppress-output`: if `true` the full Maven output will not be printed to screen (default `false`)
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
        offline: true
        arguments:
          newVersion: '#[my-second-var]'
          generateBackupPoms: 'false'
      -
        goals: 'clean install'
        suppress-output: true
    git-commit:
      branch: 'my-branch'
      pull: true
      commit-message: 'My message with variable #[my-second-var]'
```

##### Operating System Commands Action
It allows to run one or more generic operating system commands, optionally committing any resulting change to a Git repository.

Fields:
- `type`: `OPERATING_SYSTEM_COMMANDS` (required)
- `name`: action name, it must be unique for all actions (required)
- `skip-confirmation`: if `true` the util won't prompt for confirmation before running the action (default `false`)
- `custom-description`: a custom description of the action (optional), value can be [dynamic](#dynamic-value-definitions), if empty a default description will be used
- `folder`: the absolute path to the folder where the commands should be run (required), value can be [dynamic](#dynamic-value-definitions)
- `commands`: the list of commands to run (at least one required)
	- `command`: the command string (required), value can be [dynamic](#dynamic-value-definitions)
	- `suppress-output`: if `true` the full Maven output will not be printed to screen (default `false`)
- `gitCommit`: definition of the Git commit for any resulting change (optional)
	- `branch`: the Git branch (required), value can be [dynamic](#dynamic-value-definitions)
	- `commit-message`: the commit message  (required), value can be [dynamic](#dynamic-value-definitions)
	- `pull`: if `true` the util will pull the branch before running the commands (default `false`)

If a Git commit is defined, it requires a [Git configuration](#git-config).

Example:
```
  -
    name: 'My Operating System Action'
    type: 'OPERATING_SYSTEM_COMMANDS'
    folder: '~/Desktop/other-folder'
    commands:
      -
        command: 'echo "New date:" $(date +%F-%T) >> date.txt'
        suppress-output: false
    git-commit:
      branch: 'my-branch'
      pull: false
      commit-message: 'Date update'
```

##### Wait Action
It allows to wait for a specified amount of time and/or a manual user input.

Fields:
- `type`: `WAIT` (required)
- `name`: action name, it must be unique for all actions (required)
- `wait-time-milliseconds`: amount of time in milliseconds to wait (optional)
- `manual-wait-prompt`: message for a manual user input (optional), user options and question mark will be automatically added at the end

Note: if both `wait-time-milliseconds` and `manual-wait-prompt` are specified, the action will first wait for the given amount of time and then show the prompt.

Example:
```
  -
    name: 'My Wait Action'
    type: 'WAIT'
    wait-time-milliseconds: 5000
    manual-wait-prompt: 'Are you sure you want to proceed'
```

#### Procedures

Procedures allow to chain one or more actions. The user will be prompted to choose one procedure to run and then the program will proceed to execute each of its actions.

Example:
```
  -
    name: 'My First Procedure'
    steps:
      -
        type: 'RUN_ACTIONS'
        actions:
          - 'My Define Vars Action'
      -
        type: 'PICK_PROJECTS'
        custom-prompt: 'Pick one or more projects to merge'
        projects:
          - 'project1'
          - 'project2'
          - 'project3'
      -
        type: 'RUN_ACTIONS_FOR_EACH_PROJECT'
        project-actions:
          project2:
            - 'My Jenkins Action'
          default:
            - 'My Jenkins Action'
            - 'My Wait Action'
```

Procedures are made of steps. Each step is detailed in the following sections.


##### Run Actions Step
It allows to run one or more actions.

Fields:
- `type`: `RUN_ACTIONS` (required)
- `actions`: list of actions to run in sequence (at least one required)

Example:
```
      -
        type: 'RUN_ACTIONS'
        actions:
          - 'My Define Vars Action'
          - 'My Merges Action'
          - 'My Maven Action'
          - 'My Operating System Action'
```

##### Pick Projects Step
It allows to pick one or more projects from a list, to be used in subsequent [Run Actions For Each Project Steps](#run-actions-for-each-project-step).

Fields:
- `type`: `PICK_PROJECTS` (required)
- `custom-prompt`: a custom prompt for the user (optional)
- `projects`: list of projects that the user can choose from (at least one required)

Example:
```
      -
        type: 'PICK_PROJECTS'
        custom-prompt: 'Pick one or more projects for Jenkins build'
        projects:
          - 'project1'
          - 'project2'
          - 'project3'
```

##### Run Actions For Each Project Step
It allows to run one or more actions for each of the projects that were selected by the user in a preceding [Pick Projects Step](#pick-projects-step). All projects are run in sequence: first all actions for the first project are run, then all actions for the second project, etc.

Fields:
- `type`: `RUN_ACTIONS_FOR_EACH_PROJECT` (required)
- `project-actions`: map of project names and corresponding list of action names (at least one required). A `default` key can be defined: these actions will be run for each project that does not have a corresponding list of actions.

Note: actions can access the `project-name` [variable](#define-variables-action) to read the current project name.

Example:
```
      -
        type: 'RUN_ACTIONS_FOR_EACH_PROJECT'
        project-actions:
          project2:
            - 'My Jenkins Action'
          default:
            - 'My Jenkins Action'
            - 'My Wait Action'
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
- `test-mode`: if `true` all connectors are mocked and therefore no Jenkins, Git, Maven or operating system operations will be actually executed (default `false`)
- `print-passwords`: if `true` passwords will be displayed on screen (default `false`)
- `optional-pre-selected-procedure-name`: the user won't be prompted for procedure selection but instead taken directly to the specified one (optional)

Example:
```
test-mode: false
print-passwords: false
optional-pre-selected-procedure-name: 'My First Procedure'
```



### Tech documentation

The util is implemented as a Spring Boot application (Java 11, Maven).

The application **entry point** is *Runner.java*, a Spring *CommandLineRunner* that validates the properties and then runs the main logic.

**Mapping and validation** of input properties is performed by *PropertiesMapperValidator.java* and its child classes.

The **logic layer** contains all classes that orchestrate the business logic. In particular, *MainLogic.java* implements the procedure selection and then calls *ProcedureLogic.java* to run the procedure. *ProcedureLogic.java* in turn invokes the implementations of *StepLogic.java* that execute the step logic. Steps that run actions (e.g. `RunActionsStepLogic.java`) invoke the implementations of *ActionLogic.java* that execute the action logic.

The **service layer** is called by the logic layer for specific tasks. The *GitService.java* implements all Git business logic, the *JenkinsService.java* all the Jenkins business logic, the *MavenService.java* all the Maven business logic and the *OperatingSystemService.java* all the operating system business logic. The *MainService.java* is a utility service that contains references to all other services.

The **connector layer** is called by the service layer and has the task of actually connecting to external components. The *GitConnector.java* allows to connect to a Git repository (via *JGit*), the *JenkinsConnector.java* to a Jenkins server (via REST APIs), the *MavenConnector.java* to a Maven project (via *Maven Invoker*) and the *OperatingSystemConnector.java* to the OS command line (via Java *Process*).

Logic and service components all use the *CommandLineInterface.java* for **user input and output**.





