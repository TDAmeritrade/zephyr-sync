package lv.ctco.zephyr.transformer;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.testresult.junit.JUnitResult;
import lv.ctco.zephyr.beans.testresult.junit.JUnitResultTestSuite;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.enums.TestStatus;
import lv.ctco.zephyr.ZephyrSyncException;
import lv.ctco.zephyr.util.Utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JUnitTransformer implements ReportTransformer {

    private Pattern descriptionPattern;
    private Integer descriptionRegexMatchGroup;

    public String getType() {
        return "junit";
    }

    public List<TestCase> transformToTestCases(Config config) {
        String reportPath = config.getValue(ConfigProperty.REPORT_PATH);
        String fileRegex = config.getValue(ConfigProperty.FILE_REGEX);
        String descriptionRegex = config.getValue(ConfigProperty.DESCRIPTION_REGEX);
        if (descriptionRegex != null){
            this.descriptionPattern = Pattern.compile(descriptionRegex);
            String descriptionRegexMatchGroup = config.getValue(ConfigProperty.DESCRIPTION_REGEX_MATCH_GROUP);
            if (descriptionRegexMatchGroup != null){
                try {
                    this.descriptionRegexMatchGroup = Integer.valueOf(descriptionRegexMatchGroup);
                }catch(NumberFormatException e){
                    System.out.println("##### Cannot parse descriptionRegexMatchGroup: " + descriptionRegexMatchGroup +" as an int.  Using group 0.");
                    this.descriptionRegexMatchGroup = 0;
                }
            }
        }
        File path = new File(reportPath);
        if (path.isDirectory()){
            List<TestCase> list = new ArrayList<>();
            for (File file : path.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){
                    return name.matches(fileRegex);
                }
            })){
                list.addAll(transform(readJUnitReport(file.getPath())));
            }
            return list;
        }else {
            return transform(readJUnitReport(reportPath));
        }
    }

    JUnitResultTestSuite readJUnitReport(String path)  {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JUnitResultTestSuite.class);
            return (JUnitResultTestSuite) jaxbContext.createUnmarshaller().unmarshal(new File(path));
        } catch (JAXBException e) {
            throw new ZephyrSyncException("Cannot process JUnit report", e);
        }
    }

    List<TestCase> transform(JUnitResultTestSuite resultTestSuite) {
        if (resultTestSuite.getTestcase() == null) {
            return new ArrayList<TestCase>();
        }

        List<TestCase> result = new ArrayList<TestCase>();
        for (JUnitResult testCase : resultTestSuite.getTestcase()) {
            TestCase test = new TestCase();
            test.setName(testCase.getClassname()+"."+testCase.getName());
            test.setUniqueId(generateUniqueId(testCase));
            test.setStatus(testCase.getError() != null || testCase.getFailure() != null ? TestStatus.FAILED : TestStatus.PASSED);
            test.setDescription(getDescription(testCase));
            result.add(test);
        }
        return result;
    }

    String generateUniqueId(JUnitResult testCase) {
        return String.join("-", Utils.normalizeKey(testCase.getClassname()), Utils.normalizeKey(testCase.getName()));
    }

    private String getDescription(JUnitResult testCase) {
        if (descriptionPattern == null || testCase.getSystemOut() == null) {
            return "";
        }
        Matcher matcher = descriptionPattern.matcher(testCase.getSystemOut());
        if (matcher.find() && matcher.groupCount()>=descriptionRegexMatchGroup){
            return matcher.group(descriptionRegexMatchGroup);
        }
        return "";
    }
}