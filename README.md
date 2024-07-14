
## Software Releases Helper


### Description

Simple local web GUI that allows to automate some actions for software releases.

Actions include Git merges, Git pull all folders, Jenkins builds and Operating System commands. 


### Install and run

- [Download the JAR file](https://github.com/Simone3/SoftwareReleasesHelper/raw/main/downloads/SoftwareReleasesHelper.jar)
- Define your own actions (see [Configuration](#configuration))
- Run the JAR with `java -jar SoftwareReleasesHelper.jar`

Java 11 or above is required to run the util.


### Configuration

Configuration files define the custom actions to run.

This can be done with one or more standard [Spring Boot configuration files](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config).

The simplest way is to define an `application.yml` file in the same folder where the downloaded JAR is placed. As an alternative (or in addition to) the program also reads by default `application-actions.yml` and `application-configuration.yml`, but this can be overridden by defining custom Spring profiles.

See [application-sample.yml](https://raw.githubusercontent.com/Simone3/SoftwareReleasesHelper/main/software-releases-helper/src/main/resources/application-sample.yml) for a sample configuration file.

#### Actions

Actions are the building blocks of the application. They define a specific function, e.g. run a Jenkins build.

##### Jenkins Build Action

It allows to start a Jenkins build.

Fields:
- `name`: action name, it must be unique for all actions (required)
- `type`: `JENKINS_BUILD` (required)
- `jenkins-build-definition.url`: the relative Jenkins URL (required), value can [contain placeholders](#variables)
- `jenkins-build-definition.parameters`: key-value map of build parameters (optional), keys and values can [contain placeholders](#variables)

It requires a global [Jenkins configuration](#jenkins-config).

Example:
```
  -
    name: 'Sample Jenkins Action 2'
    type: JENKINS_BUILD
    jenkins-build-definition:
      url: '/job/my-location/job/my-job/buildWithParameters'
      parameters:
      -
        key: 'project'
        value: 'my-project'
      -
        key: 'version'
        value: '1.0.0-SNAPSHOT'
```

##### Git Merges Action

It allows to perform one or more Git merges.

Fields:
- `name`: action name, it must be unique for all actions (required)
- `type`: `GIT_MERGES` (required)
- `git-merges-definition.repository-folder`: the absolute or relative path to the repository folder (required), value can [contain placeholders](#variables)
- `git-merges-definition.merges`: the merges to perform, defined by two or more branches separated by `->`, with multiple merge steps separated by `;` (e.g. `branch1 -> branch2 -> branch3; branch4 -> branch5` means that `branch1` will be merged into `branch2`, then `branch2` into `branch3`, then `branch4` into `branch5`), values can [contain placeholders](#variables)
- `git-merges-definition.pull`: if `true` the util will pull all source and target branches before all merges (default `false`)

It requires a global [Git configuration](#git-config).

Example:
```
  -
    name: 'Sample Git Merges Action 1'
    type: GIT_MERGES
    git-merges-definition:
      repository-folder: '/my-folder/my-other-folder/'
      merges: 'develop -> release -> master; hotfix -> master'
      pull: true
```

##### Git Pull All Action

It allows to pull all Git repositories inside a parent folder (any nested level).

Fields:
- `name`: action name, it must be unique for all actions (required)
- `type`: `GIT_PULL_ALL` (required)
- `git-pull-all-definition.parent-folder`: the absolute path of the parent folder (required), value can [contain placeholders](#variables)
- `git-pull-all-definition.skip-if-working-tree-dirty`: if `true`, any repository with uncommitted changes will be skipped (default `false`)

It requires a global [Git configuration](#git-config).

Example:
```
  -
    name: 'Sample Git Pull All Action 1'
    type: GIT_PULL_ALL
    git-pull-all-definition:
      parent-folder: '/dev/my-folder'
      skip-if-working-tree-dirty: false
```

##### Operating System Commands Action

It allows to run one or more generic operating system commands, optionally committing any resulting change to a Git repository.

Fields:
- `name`: action name, it must be unique for all actions (required)
- `type`: `OPERATING_SYSTEM_COMMANDS` (required)
- `os-commands-definition.folder`: the absolute path to the folder where the commands should be run (required), value can [contain placeholders](#variables)
- `os-commands-definition.commands`: the list of commands to run (at least one required)
	- `command`: the command string (required), value can [contain placeholders](#variables)
	- `suppress-output`: if `true` the full command output will not be printed to screen (default `false`)
- `os-commands-definition.gitCommit`: definition of the Git commit for any resulting change (optional)
	- `branch`: the Git branch (required), value can [contain placeholders](#variables)
	- `commit-message`: the commit message  (required), value can [contain placeholders](#variables)
	- `pull`: if `true` the util will pull the branch before running the commands (default `false`)

If a Git commit is defined, it requires a [Git configuration](#git-config).

Example:
```
  -
    name: 'Sample OS Commands Action 1'
    type: OPERATING_SYSTEM_COMMANDS
    os-commands-definition:
      folder: '~/Desktop/my-repos/#[project]'
      commands:
      -
        command: 'mvn clean install'
        suppress-output: false
      -
        command: 'echo "some message" > tracker.txt'
        suppress-output: true
      git-commit:
        branch: develop
        commit-message: 'This is a custom commit message'
        pull: true
```

#### Variables

Some fields that define actions can contain placeholders, whose values are dynamically defined by variables. Variables can be defined with the `variables` field of each action.

Example:
```
  -
    name: 'Sample Jenkins Action 1'
    type: JENKINS_BUILD
    variables:
      -
        key: 'environment'
        type: STRICT_SELECT
        value: 'env-1'
        options:
          - 'env-1'
          - 'env-2'
          - 'env-3'
      -
        key: 'project'
        type: FREE_SELECT
        remove-whitespace: true
        value: 'prj-2'
        options:
          - 'prj-1'
          - 'prj-2'
      -
        key: 'version'
        type: TEXT
        remove-whitespace: true
        value: '1.0.0'
      -
        key: 'application'
        type: STATIC
        value: 'a fixed variable'
    jenkins-build-definition:
      url: '/job/#[environment]/job/#[project]/buildWithParameters'
      parameters:
      -
        key: 'version'
        value: '#[version]'
      -
        key: 'build_version'
        value: '1.2.3'
      -
        key: 'app'
        value: '#[application]'
      -
        key: 'description'
        value: 'build of #[project] on environment #[environment]'
```

##### Text variable
A text input.

Fields:
- `key`: variable key, it must be unique for all variables (required)
- `type`: `TEXT` (required)
- `remove-whitespace`: if `true` the user cannot input whitespace characters (default `false`)
- `value`: default value (optional)

Example:
```
      -
        key: 'version'
        type: TEXT
        remove-whitespace: true
        value: '1.0.0'
```

##### Strict select variable
A dropdown input.

Fields:
- `key`: variable key, it must be unique for all variables (required)
- `type`: `STRICT_SELECT` (required)
- `value`: default value (optional)
- `options`: list of select options (at least one required)

Example:
```
      -
        key: 'environment'
        type: STRICT_SELECT
        value: 'env-1'
        options:
          - 'env-1'
          - 'env-2'
          - 'env-3'
```


##### Free select variable

A dropdown input that also allows to write free text, i.e. a text input with suggestions.

Fields:
- `key`: variable key, it must be unique for all variables (required)
- `type`: `FREE_SELECT` (required)
- `remove-whitespace`: if `true` the user cannot input whitespace characters (default `false`)
- `value`: default value (optional)
- `options`: list of select options (at least one required)

Example:
```
      -
        key: 'project'
        type: FREE_SELECT
        remove-whitespace: true
        value: 'prj-2'
        options:
          - 'prj-1'
          - 'prj-2'
```

##### Static variable
An input that cannot be changed, just for reuse purposes.

Fields:
- `key`: variable key, it must be unique for all variables (required)
- `type`: `STATIC` (required)
- `value`: the static value (required)

Example:
```
      -
        key: 'application'
        type: STATIC
        value: 'a fixed variable'
```

#### Global configs

##### Main config

Fields:
- `test-mode`: if `true` all connectors are mocked and therefore no Jenkins, Git or operating system operations will be actually executed (default `false`)
- `print-passwords`: if `true` passwords will be displayed on screen (default `false`)
- `web-gui`: for now, an unused config (default `true`)

Example:
```
test-mode: false
print-passwords: false
web-gui: true
```

##### Git config
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

##### Jenkins config
Global Jenkins configuration. It is required if at least one action is Jenkins-related.

Fields:
- `base-url`: the base URL for all Jenkins URLs (required)
- `use-crumb`: if a crumb is required to run builds (default `false`)
- `crumb-url`: the relative URL of the Jenkins Crumb Issuer (required if `use-crumb` is `true`)
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

### Tech documentation

#### Back-end

The util back-end layer is implemented as a Spring Boot application (Java 11, Maven).

The application **startup helpers** are *GlobalContext.java* and *Runner.java*, that validate the properties and print CLI info messages.

**Mapping and validation** of input properties is performed by *PropertiesMapperValidator.java* and its child classes.

The **logic layer** contains all classes that orchestrate the business logic. In particular
- *InitSessionLogic.java* sends the domain values to the front-end.
- *ActionLogic.java* and its subclasses execute each action logic

The **connector layer** is called by the logic layer and has the task of actually connecting to external components. The *GitConnector.java* allows to connect to a Git repository (via *JGit*), the *JenkinsConnector.java* to a Jenkins server (via REST APIs) and the *OperatingSystemConnector.java* to the OS command line (via Java *Process*).

Logic components all use the *ViewAdapter.java* abstraction for **user input and output**. In particular
- *WebSocketInboundAdapter.java* receives WebSocket events from the web front-end and triggers the correct *Logic* component
- *WebSocketOutboundAdapter.java* sends WebSocket events to the web front-end 

#### Web front-end

The util web front-end layer is implemented as a React application.

*MainPage.js* is the main entry point, whose purpose is to send and receive WebSocket events and holds the main centralized state.

*ActionsList.js* is the main initial list of actions.

*ActionDetails.js* allows to view and run a single action.

*HistoryList.js* is the sidebar with the history messages.
