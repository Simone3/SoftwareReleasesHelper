
# Software Releases Helper

Simple utility that allows to automate some actions for software releases.

At the moment, it allows to define and run with a Command Line Interface:
- Git merge operations
- Jenkins builds startup

Actions are grouped by category and project, with multiple-selection and chaining options.

To run the utility, simply:
- [Download the JAR file](https://github.com/Simone3/SoftwareReleasesHelper/raw/main/downloads/SoftwareReleasesHelper.jar)
- Define your own Git and/or Jenkins actions by placing an `application.yml` file in the JAR folder (see [application-sample.yml](https://github.com/Simone3/SoftwareReleasesHelper/blob/main/src/main/resources/application-sample.yml) for an example)
- Run the JAR with `java -jar SoftwareReleasesHelper.jar`

