package lv.ctco.zephyr;

import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.enums.TestStatus;
import lv.ctco.zephyr.service.ZephyrService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ZephySyncServiceTest {

    @Test
    public void testConsolidatedTests(){
        ZephyrService service = new ZephyrService(null);
        List<TestCase> results = service.consolidateTestCases(buildTestCases());
        Assert.assertEquals("Should have 2 items", 2, results.size());
        Assert.assertEquals("comment should be correct", "Total Items: 3 \n" +
                "Total Passed: 1 \n" +
                "Total Failed: 2 \n" +
                "Failed Items: [1, 3]", results.get(1).getExecutionSummary());
        Assert.assertEquals("1st test should be passed", TestStatus.PASSED, results.get(0).getStatus());
        Assert.assertEquals("2nd test should be failed", TestStatus.FAILED, results.get(1).getStatus());
    }

    private List<TestCase> buildTestCases(){
        List<TestCase> list = new ArrayList<>();
        TestCase testCase1 = new TestCase();
        testCase1.setName("anotherMethod");
        testCase1.setStatus(TestStatus.PASSED);
        list.add(testCase1);
        TestCase testCase2 = new TestCase();
        testCase2.setName("testMethod(String)[2]");
        testCase2.setStatus(TestStatus.PASSED);
        list.add(testCase2);
        TestCase testCase3 = new TestCase();
        testCase3.setName("testMethod(String)[3]");
        testCase3.setStatus(TestStatus.FAILED);
        list.add(testCase3);
        TestCase testCase4 = new TestCase();
        testCase4.setName("testMethod(String)[1]");
        testCase4.setStatus(TestStatus.FAILED);
        list.add(testCase4);
        return list;
    }

}
