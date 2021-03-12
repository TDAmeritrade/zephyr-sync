package lv.ctco.zephyr.beans;

import lv.ctco.zephyr.enums.TestLevel;
import lv.ctco.zephyr.enums.TestStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCase implements Comparable<TestCase>, Cloneable{

    private Integer id;

    @Override
    public Object clone(){
        try {
            return super.clone();
        }catch(CloneNotSupportedException e){
            return this;
        }
    }

    private String key;
    private String uniqueId;
    private String suiteName;
    private String name;
    private String description;
    private List<String> storyKeys;
    private List<String> labels;
    private List<TestStep> steps;
    private TestStatus status = TestStatus.NOT_EXECUTED;
    private TestLevel severity;
    private TestLevel priority = TestLevel.MEDIUM;
    private String executionSummary;
    Pattern consolidatedNamePattern = Pattern.compile("\\[(\\d+)\\]$");

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStoryKeys() {
        return storyKeys;
    }

    public void setStoryKeys(List<String> storyKeys) {
        this.storyKeys = storyKeys;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    public void setSteps(List<TestStep> steps) {
        this.steps = steps;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public TestLevel getSeverity() {
        return severity;
    }

    public void setSeverity(TestLevel severity) {
        this.severity = severity;
    }

    public TestLevel getPriority() {
        return priority;
    }

    public void setPriority(TestLevel priority) {
        this.priority = priority;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(String executionSummary) {
        this.executionSummary = executionSummary;
    }

    @Override
    public int compareTo(TestCase o) {
        return this.getName() == null ? 0 : this.getName().compareTo(o.getName());
    }

    public String getConsolidatedName(){
        return getName() == null ? null : getName().replaceAll(consolidatedNamePattern.pattern(), "").trim();
    }

    public String getConsolidatedArrayLocation(){
        Matcher matcher = consolidatedNamePattern.matcher(getName());
        if (matcher.find() && matcher.groupCount()>0){
            return matcher.group(1);
        }else{
            return "";
        }
    }

}
