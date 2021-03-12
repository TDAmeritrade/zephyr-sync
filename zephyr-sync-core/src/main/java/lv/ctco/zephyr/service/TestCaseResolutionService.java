package lv.ctco.zephyr.service;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.transformer.ReportTransformer;
import lv.ctco.zephyr.transformer.ReportTransformerFactory;
import lv.ctco.zephyr.ZephyrSyncException;

import java.util.Iterator;
import java.util.List;

public class TestCaseResolutionService {

    private Config config;

    public TestCaseResolutionService(Config config) {
        this.config = config;
    }

    public List<TestCase> resolveTestCases() {
        String reportType = config.getValue(ConfigProperty.REPORT_TYPE);
        ReportTransformer transformer = ReportTransformerFactory.getInstance().getTransformer(reportType);
        List<TestCase> testCases = transformer.transformToTestCases(config);
        if (testCases == null) {
            throw new ZephyrSyncException("No Test Cases extracted from the Test Report");
        }
        for (Iterator<TestCase> it = testCases.iterator(); it.hasNext(); ) {
            TestCase testCase = it.next();
            if (testCase.getName() == null || testCase.getName().length() == 0) {
                it.remove();
            }
        }
        if (testCases.isEmpty()) {
            throw new ZephyrSyncException("No Test Cases extracted from the Test Report");
        }
        return testCases;
    }
}
