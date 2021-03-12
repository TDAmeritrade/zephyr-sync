package lv.ctco.zephyr.transformer;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.TestStep;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.enums.TestLevel;
import lv.ctco.zephyr.enums.TestStatus;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class AllureTransformerTest {

    AllureTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new AllureTransformer();
    }

    @Test
    public void transformToTestCases() throws URISyntaxException {
        Config config = new Config();
        Path reportPath = Paths.get(AllureTransformerTest.class.getResource("/reports").toURI());
        config.setValue(ConfigProperty.REPORT_PATH, reportPath.toString());
        List<TestCase> testCases = transformer.transformToTestCases(config);
        assertThat(testCases, hasSize(1));

        TestCase testCase = testCases.get(0);
        assertThat(testCase.getName(), is("Account Correlation from Source"));
        assertThat(testCase.getDescription(), is("Free text"));
        assertThat(testCase.getUniqueId(), is("Account Correlation from Source"));
        assertThat(testCase.getStoryKeys(), containsInAnyOrder("EPIC-1111", "FEAT-555", "STRY-9999", "STRY-1123"));
        assertThat(testCase.getKey(), is(nullValue()));
        assertThat(testCase.getStatus(), CoreMatchers.is(TestStatus.PASSED));
        assertThat(testCase.getPriority(), CoreMatchers.is(TestLevel.MEDIUM));
        assertThat(testCase.getSeverity(), is(nullValue()));
        assertThat(testCase.getLabels(), containsInAnyOrder("any-label", "another-label", "SomeTags"));
        assertThat(testCase.getSteps(), hasSize(3));

        TestStep firstStep = testCase.getSteps().get(0);
        assertThat(firstStep.getDescription(), is("variable \"attrName\" is created with value \"X213DDF\""));
        assertThat(firstStep.getSteps(), is(empty()));

        TestStep secondStep = testCase.getSteps().get(1);
        assertThat(secondStep.getDescription(), is("Account 'X213DDF' is created"));
        assertThat(secondStep.getSteps(), is(empty()));

        TestStep thirdNestedStep = testCase.getSteps().get(2);
        assertThat(thirdNestedStep.getDescription(), is("When something happens"));
        assertThat(thirdNestedStep.getSteps(), hasSize(6));

    }
}