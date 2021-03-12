# Zephyr Sync

## Overview

Zephyr Sync is a tool, that allows your project to perform synchronization of automated test results to Zephyr - a JIRA addon for Test Management. The advanced configuration of the tool supports multiple report types to work with, as well as some restrictions to be applied during the sync.

## Usage example

### Maven

All changes should be done inside `pom.xml`.

#### Using zephyr-sync-maven-plugin (recommended)

The configuration is very simple and should be done in `pom.xml` 
The maven-surefire-plugin must be configured to ignore test failures otherwise this plugin will never get hit.  By default the plugin will 
(note that this example is given for `junit`, other reports like `cucumber` are configured in the similar way):

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M5</version>
    <configuration>
        <testFailureIgnore>true</testFailureIgnore>
    </configuration>
</plugin>
<plugin>
    <groupId>lv.ctco.zephyr</groupId>
    <artifactId>zephyr-sync-maven-plugin</artifactId>
    <version>${zephyr-sync.version}</version>
    <dependencies>
        <dependency>
            <groupId>lv.ctco.zephyr</groupId>
            <artifactId>zephyr-sync-report-junit</artifactId>
            <version>${zephyr-sync.version}</version>
        </dependency>
    </dependencies>
    <configuration>
        <username>zjirauser</username>
        <password>${env.ZJIRAUSER_PWD}</password>
        <reportType>junit</reportType>
        <projectKey>XYZ</projectKey>
        <jiraUrl>https://jira.associatesys.local/rest/</jiraUrl>
        <reportPath>${project.build.directory}/surefire-reports/</reportPath>
        <useGitBranchForCycle>true</useGitBranchForCycle>
        <descriptionRegex>\[description\](.+?)\[\/description\]</descriptionRegex>
        <descriptionRegexMatchGroup>1</descriptionRegexMatchGroup>
    </configuration>
    <executions>
        <execution>
        <phase>test</phase>
        <goals>
            <goal>sync</goal>
        </goals>
        </execution>
    </executions>
</plugin>
```

#### Using maven-exec-plugin (deprecated)
First of all - declare dependency to `zephyr-sync-core`:

```
<dependencies>
    ...
    <dependency>
        <groupId>lv.ctco.zephyr</groupId>
        <artifactId>zephyr-sync-core</artifactId>
        <version>${zephyr-sync.version}</version>
    </dependency>
</dependencies>
```

Also configure a Maven plugin that will trigger synchronization to JIRA:

```
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.3.1</version>
    <executions>
        <execution>
            <id>default-cli</id>
            <goals>
                <goal>java</goal>
            </goals>
            <configuration>
                <mainClass>Runner</mainClass>
                <arguments>
                    <argument>--username=zjirauser</argument>
                    <argument>--password=${env.ZJIRAUSER_PWD}</argument>
                    <argument>--reportType=junit</argument>
                    <argument>--projectKey=XYZ</argument>
                    <argument>--releaseVersion=1.0</argument>
                    <argument>--jiraUrl=http://jira.yourcompany.com/rest/</argument>
                    <argument>--reportPath=${project.build.directory}/surefire-reports/</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>

```

This example shows only minimal set of mandatory attributes.
For complete list of attributes refer to sections below.

### Command Line Interface

```
java -jar zephyr-sync-cli-${zephyr-sync.version}-all-in-one.jar --username=zjirauser --password=123456 --reportType=junit --projectKey=ABC --releaseVersion="Release 2.1" --jiraUrl=http://jira.yourcompany.com/rest/ --reportPath=build/surefire-reports/
```

### Using Gradle (using CLI)
```
task zephyrSync {
         javaexec {
            main = "Runner"
            classpath = sourceSets.main.output + sourceSets.test.output
            args = ["--username=zjiruauser", "--password=123456", "--reportType=junit", "--projectKey=ABC",
                    "--releaseVersion=Release 2.1", "--jiraUrl=http://jira.yourcompany.com/rest/", "--reportPath=build/surefire-reports/"]
        }
    }
```

## Configuration properties

This is the list of possible configuration items:

Property                      | Meaning                                                                                                                                                                                                                                                                                  | Is mandatory? | Default value | Example
---                           | ---                                                                                                                                                                                                                                                                                      | ---           | ---           | ---
username                      | User name used to connect to JIRA                                                                                                                                                                                                                                                        | yes           |               | `zjirauser`
password                      | Password for the user to connect to JIRA                                                                                                                                                                                                                                                 | yes           |               | `password`
reportType                    | Type of report that will be synchronized to Zephyr                                                                                                                                                                                                                                       | yes           |               | One of `cucumber`, `allure`, `junit` or `nunit`
projectKey                    | Key of project in JIRA                                                                                                                                                                                                                                                                   | yes           |               | `XYZ`
jiraUrl                       | URL of JIRA (it's RESTful API endpoint)                                                                                                                                                                                                                                                  | yes           |               | `https://jira.associatesys.local/rest/`
reportPath                    | Path on the file system where reports are stored. Can be a file or a folder.                                                                                                                                                                                                             | yes           |               | For junit: `${project.build.directory}/surefire-reports/`
testCycle                     | Zephyr test cycle where the results will be linked to                                                                                                                                                                                                                                    | no            | `Unkown`      | `TEST CYCLE 1`
applicationName               | Used to prefix git branch when using git branch for cycle.                                                                                                                                                                                                                               | no            |               |
releaseVersion                | FixVersion of a project to link Test results to                                                                                                                                                                                                                                          | no            |               | `CAP 21.01`
fileRegex                     | Regex for files names in reportPath foldere                                                                                                                                                                                                                                              | no            | `TEST.*\.xml` | `TEST.*\.xml`
orderedSteps                  | If set to true, numerical prefix for test steps will be put (hierarchical)                                                                                                                                                                                                               | no            | `false`       |
forceStoryLink                | If set to true, sync will be failed in case at least one test doesn't have @Stories=ABC-XXX annotation                                                                                                                                                                                   | no            | `true`        |
generateTestCaseUniqueId      | Name of JIRA attribute that is used to store unique ID of test case (will be used for test case tracking, updates and linking)                                                                                                                                                           | no            | `false`       |
severityAttribute             | Name of JIRA attribute that stores 'Severity' attribute                                                                                                                                                                                                                                  | no            |               |
autoCreateTestCycle           | Should new test cycle be created automatically                                                                                                                                                                                                                                           | no            | `true`        |
assignee                      | Specify an Assignee                                                                                                                                                                                                                                                                      | no            |               |
linkType                      | Link type between Test issue and related story (used in combination with `@Stories` annotation)                                                                                                                                                                                          | no            | `Reference`   |
linkDirection                 | Link direction between Test issue and related story                                                                                                                                                                                                                                      | no            | `inward`      | One of `inward` or `outward`
useGitBranchForCycle          | Automatically detect current GIT branch and use that for the Test Cycle                                                                                                                                                                                                                  | no            | `true`        | 
updateIssue                   | Update any test issues that have already been created                                                                                                                                                                                                                                    | no            | `false`       |
descriptionRegex              | Regex to use to capture Test Issue description from system log. (only currently works with test type `junit`).  Add descriptions in your Junit tests by printing them out using System.out.println. e.g. `System.out.println("[description]I'm a unit test description[/description]");` | no            |               | `\[description\](.+?)\[\/description\]`
descriptionRegexMatchGroup    | Which group in the regex match to use when looking for the description in the log. (only currently works with test type `junit`)                                                                                                                                                         | no            |               | `1`
failBuildOnTestFailure        | Fail the build if any unit tests fail.  (Note: Maven Surefire plugin fails the build after test failure.  To let the build continue and hit this plugin, enable testFailureIgnore in Maven Surefire)                                                                                     | no            | `true`        |
consolidateParameterizedTests | Consolidate parameterized unit tests into one Jira Test issue.                                                                                                                                                                                                                           | no            | `true`        | 