package lv.ctco.zephyr;

import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.enums.TestStatus;
import lv.ctco.zephyr.service.*;
import lv.ctco.zephyr.util.CustomPropertyNamingStrategy;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.service.*;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.util.List;

public class ZephyrSyncService {

    //private AuthService authService;
    private MetaInfoRetrievalService metaInfoRetrievalService;
    private TestCaseResolutionService testCaseResolutionService;
    private JiraService jiraService;
    private ZephyrService zephyrService;
    boolean failBuildOnTestFailure;
    Config config;

    public ZephyrSyncService(Config config) {
        this.config = config;
        ObjectTransformer.setPropertyNamingStrategy(new CustomPropertyNamingStrategy(config));

        //authService = new AuthService(config);
        metaInfoRetrievalService = new MetaInfoRetrievalService(config);
        testCaseResolutionService = new TestCaseResolutionService(config);
        jiraService = new JiraService(config);
        zephyrService = new ZephyrService(config);

        failBuildOnTestFailure = Boolean.valueOf(config.getValue(ConfigProperty.FAIL_BUILD_ON_TEST_FAILURE));
    }

    public void execute() throws IOException, InterruptedException {
        //authService.authenticateInJira();

        MetaInfo metaInfo = metaInfoRetrievalService.retrieve();

        List<TestCase> testCases = testCaseResolutionService.resolveTestCases();
        List<Issue> issues = jiraService.getTestIssues();

        zephyrService.mapTestCasesToIssues(testCases, issues);

        for (TestCase testCase : testCases) {
            if (!"true".equals(config.getValue(ConfigProperty.CONSOLIDATE_PARAMETERIZED_TESTS))
                    || (!testCase.getConsolidatedName().equals(testCase.getName()) && testCase.getConsolidatedArrayLocation().equals("1"))) {
                if (testCase.getId() == null) {
                    jiraService.createTestIssue(testCase);
                    zephyrService.addStepsToTestIssue(testCase);
                    jiraService.linkToStory(testCase);
                }
                boolean updateIssue = Boolean.valueOf(config.getValue(ConfigProperty.UPDATE_ISSUE));
                if (updateIssue) {
                    jiraService.updateTestIssue(testCase);
                }
            }

        }

        zephyrService.linkExecutionsToTestCycle(metaInfo, testCases);
        zephyrService.updateExecutionStatuses(testCases);
        if (failBuildOnTestFailure){
            boolean hasFailed = false;
            for (TestCase testCase : testCases){
                if (testCase.getStatus() == TestStatus.FAILED){
                    hasFailed = true;
                    break;
                }
            }
            if (hasFailed){
                throw new ZephyrSyncException("There are unit test failures.  Marking build as failed.");
            }
        }

    }
}
