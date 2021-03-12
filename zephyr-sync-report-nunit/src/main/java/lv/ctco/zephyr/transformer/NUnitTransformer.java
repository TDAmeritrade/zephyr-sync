package lv.ctco.zephyr.transformer;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.ZephyrSyncException;
import lv.ctco.zephyr.beans.testresult.nunit.NUnitResultTestRun;
import lv.ctco.zephyr.beans.testresult.nunit.NUnitTestCase;
import lv.ctco.zephyr.beans.testresult.nunit.NUnitTestSuite;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.enums.TestStatus;

import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.List;


public class NUnitTransformer implements ReportTransformer {

    private static final String NUNIT_PASSED = "Passed";

    public String getType() {
        return "nunit";
    }

    public List<TestCase> transformToTestCases(Config config) {

        return transform(readNunitReport(config.getValue(ConfigProperty.REPORT_PATH)));
    }

    NUnitResultTestRun readNunitReport(String path) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NUnitResultTestRun.class);

            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(path));
            xsr = xif.createFilteredReader(xsr, r -> r.getEventType() != XMLStreamReader.CHARACTERS || r.getText().trim().length() > 0);
            return (NUnitResultTestRun) jaxbContext.createUnmarshaller().unmarshal(xsr);
        } catch (Exception e) {
            throw new ZephyrSyncException("Cannot process NUnit report", e);
        }
    }

    List<TestCase> transform(NUnitResultTestRun resultTestSuite) {
        // NUnit places project name to the first test suite
        List<NUnitTestSuite> nUnitTestSuites = resultTestSuite.getTestSuite().stream().findFirst().get().flattenTestSuite();
        List<TestCase> result = new ArrayList<>();
        for (NUnitTestSuite testSuite : nUnitTestSuites) {
            for (NUnitTestCase testCase : testSuite.getTestCases()) {
                TestCase test = new TestCase();
                test.setName(testCase.getName());
                test.setUniqueId(testCase.getId());
                test.setStatus(NUNIT_PASSED.equals(testCase.getResult()) ? TestStatus.PASSED : TestStatus.FAILED);
                result.add(test);
            }
        }
        return result;
    }

}