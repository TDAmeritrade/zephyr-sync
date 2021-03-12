package lv.ctco.zephyr.enums;

import lv.ctco.zephyr.ZephyrSyncException;

public enum ConfigProperty {
    USERNAME("username", true),
    PASSWORD("password", true),
    REPORT_TYPE("reportType", true),
    PROJECT_KEY("projectKey", true),
    RELEASE_VERSION("releaseVersion", false),
    TEST_CYCLE("testCycle", false, "Unknown"),
    JIRA_URL("jiraUrl", true),
    REPORT_PATH("reportPath", true),
    FILE_REGEX("fileRegex", false, "TEST.*\\.xml"),
    ORDERED_STEPS("orderedSteps", false, "false"),
    FORCE_STORY_LINK("forceStoryLink", false, "true"),
    TEST_CASE_UNIQUE_ID("testCaseUniqueId", false),
    GENERATE_TEST_CASE_UNIQUE_ID("generateTestCaseUniqueId", false, "false"),
    SEVERITY("severityAttribute", false),
    AUTO_CREATE_TEST_CYCLE("autoCreateTestCycle", false, "true"),
    ASSIGNEE("assignee", false),
    LINK_TYPE("linkType", false, "Reference"),
    LINK_DIRECTION("linkDirection", false, "inward"),
    USE_GIT_BRANCH_FOR_CYCLE("useBranchForCycle", false, "true"),
    FAIL_BUILD_ON_TEST_FAILURE( "failBuildOnTestFailure", false, "true"),
    UPDATE_ISSUE("updateIssue", false, "false"),
    DESCRIPTION_REGEX("descriptionRegex", false),
    DESCRIPTION_REGEX_MATCH_GROUP( "descriptionRegexMatchGroup", false, "0"),
    APPLICATION_NAME( "applicationName", false),
    CONSOLIDATE_PARAMETERIZED_TESTS( "consolidateParameterizedTests", false, "true");

    private String propertyName;
    private boolean mandatory;
    private String defaultValue;

    ConfigProperty(String propertyName, boolean mandatory) {
        this.propertyName = propertyName;
        this.mandatory = mandatory;
    }

    ConfigProperty(String propertyName, boolean mandatory, String defaultValue) {
        this(propertyName, mandatory);
        this.defaultValue = defaultValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static ConfigProperty findByName(String name) {
        for (ConfigProperty property : values()) {
            if (property.getPropertyName().equalsIgnoreCase(name)) {
                return property;
            }
        }
        throw new ZephyrSyncException("Unsupported parameter is passed " + name);
    }
}