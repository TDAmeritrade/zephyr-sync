package lv.ctco.zephyr.transformer;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.TestStep;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.enums.TestLevel;
import lv.ctco.zephyr.enums.TestStatus;
import io.qameta.allure.AllureResultsReader;
import io.qameta.allure.FileSystemResultsReader;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.util.ResultsUtils.EPIC_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.FEATURE_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.SEVERITY_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.STORY_LABEL_NAME;
import static io.qameta.allure.util.ResultsUtils.TAG_LABEL_NAME;

public class AllureTransformer implements ReportTransformer {

    private static final List<String> STORY_LABELS = Arrays.asList(EPIC_LABEL_NAME, FEATURE_LABEL_NAME, STORY_LABEL_NAME);
    private static final List<String> LABEL_LABELS = Arrays.asList("label", TAG_LABEL_NAME);

    @Override
    public String getType() {
        return "allure";
    }

    @Override
    public List<TestCase> transformToTestCases(Config config) {
        return transform(readAllureReport(config.getValue(ConfigProperty.REPORT_PATH)));
    }

    private Stream<TestResult> readAllureReport(String path) {
        AllureResultsReader reader = new FileSystemResultsReader(Paths.get(path));
        return reader.readTestResults();
    }

    private List<TestCase> transform(Stream<TestResult> results) {
        return results.map(this::transform).collect(Collectors.toList());
    }

    private TestCase transform(TestResult testResult) {
        TestCase currentTestCase = new TestCase();
        currentTestCase.setName(testResult.getName());
        currentTestCase.setUniqueId(generateUniqueId(testResult));
        currentTestCase.setDescription(testResult.getDescription());
        currentTestCase.setStoryKeys(getStoryKeys(testResult));
        currentTestCase.setStatus(getStatus(testResult));
        currentTestCase.setSeverity(getSeverity(testResult));
        currentTestCase.setLabels(getLabels(testResult));
        currentTestCase.setSteps(addTestSteps(testResult.getSteps(), 1));
        return currentTestCase;
    }

    private String generateUniqueId(TestResult testResult) {
        return testResult.getName();
    }

    private TestStatus getStatus(TestResult testResult) {
        switch (testResult.getStatus()) {
            case FAILED:
                return TestStatus.FAILED;
            case BROKEN:
                return TestStatus.FAILED;
            case PASSED:
                return TestStatus.PASSED;
            default:
                return TestStatus.NOT_EXECUTED;
        }
    }

    private TestLevel getSeverity(TestResult testResult) {
        String severity = "";

        for (Label currentLabel : testResult.getLabels()) {
            if (currentLabel.getName().equalsIgnoreCase(SEVERITY_LABEL_NAME) && !currentLabel.getValue().isEmpty()) {
                severity = currentLabel.getValue();
            }
        }
        if (!(severity.isEmpty())) {
            switch (fromValue(severity)) {
                case TRIVIAL:
                    return TestLevel.TRIVIAL;
                case MINOR:
                    return TestLevel.MINOR;
                case CRITICAL:
                    return TestLevel.CRITICAL;
                case BLOCKER:
                    return TestLevel.BLOCKER;
                default:
                    return TestLevel.MAJOR;
            }
        }
        return null;
    }

    private SeverityLevel fromValue(String severity) {
        return Stream.of(SeverityLevel.values())
                .filter(level -> level.value().equals(severity))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported severity level: "+ severity));
    }

    private List<String> getStoryKeys(TestResult testResult) {
        return getAllureLabels(testResult, label -> STORY_LABELS.contains(label.getName()));
    }

    private List<String> getLabels(TestResult testResult) {
        return getAllureLabels(testResult, label -> LABEL_LABELS.contains(label.getName()));
    }

    private List<String> getAllureLabels(TestResult testResult, Predicate<Label> predicate) {
        Set<String> stories = testResult.getLabels().stream()
                .filter(predicate)
                .filter(label -> !label.getValue().isEmpty())
                .map(Label::getValue)
                .collect(Collectors.toSet());
        return stories.isEmpty() ? null : new ArrayList<>(stories);
    }

    private List<TestStep> addTestSteps(List<StepResult> steps, int level) {
        List<TestStep> result = new ArrayList<TestStep>(steps.size());
        for (StepResult stepResult : steps) {
            TestStep testStep = new TestStep();
            testStep.setDescription(stepResult.getName());
            testStep.setSteps(addTestSteps(stepResult.getSteps(), level + 1));
            result.add(testStep);
        }
        return result;
    }
}