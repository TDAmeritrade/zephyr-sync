package lv.ctco.zephyr.service;

import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.Metafield;
import lv.ctco.zephyr.beans.TestCase;
import lv.ctco.zephyr.beans.jira.Issue;
import lv.ctco.zephyr.beans.jira.IssueLink;
import lv.ctco.zephyr.beans.jira.IssueLinkDirection;
import lv.ctco.zephyr.beans.jira.SearchResponse;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.transformer.TestCaseToIssueTransformer;
import lv.ctco.zephyr.util.HttpUtils;
import lv.ctco.zephyr.ZephyrSyncException;
import lv.ctco.zephyr.util.ObjectTransformer;
import com.google.api.client.http.HttpResponse;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static lv.ctco.zephyr.util.Utils.log;
import static lv.ctco.zephyr.util.Utils.readInputStream;

public class JiraService {

    private static final int TOP = 500;

    private Config config;

    public JiraService(Config config) {
        this.config = config;
    }

    public List<Issue> getTestIssues() throws IOException {
        int skip = 0;
        log("Fetching JIRA Test issues for the project");
        String search = "project='" + config.getValue(ConfigProperty.PROJECT_KEY) + "'%20and%20issueType=Test";
        SearchResponse searchResults = searchInJQL(search, skip);
        if (searchResults == null || searchResults.getIssues() == null) {
            throw new ZephyrSyncException("Unable to fetch JIRA test issues");
        }

        List<Issue> issues = searchResults.getIssues();

        int totalCount = searchResults.getTotal();
        if (totalCount > TOP) {
            while (issues.size() != totalCount) {
                skip += TOP;
                issues.addAll(searchInJQL(search, skip).getIssues());
            }
        }
        log(format("Retrieved %s Test issues\n", issues.size()));
        return issues;
    }

    SearchResponse searchInJQL(String search, int skip) throws IOException {
        String response = HttpUtils.getAndReturnBody(config, "api/2/search?jql=" + search + "&maxResults=" + TOP + "&startAt=" + skip);
        return ObjectTransformer.deserialize(response, SearchResponse.class);
    }

    public void createTestIssue(TestCase testCase) throws IOException {
        log("INFO: Creating JIRA Test item with Name: \"" + ("true".equals(config.getValue(ConfigProperty.CONSOLIDATE_PARAMETERIZED_TESTS)) ? testCase.getConsolidatedName() : testCase.getName()) + "\".");
        Issue issue = TestCaseToIssueTransformer.transform(config, testCase);

        HttpResponse response = HttpUtils.post(config, "api/2/issue", issue);
        HttpUtils.ensureResponse(response, 201, "ERROR: Could not create JIRA Test item");

        String responseBody = readInputStream(response.getContent());
        Metafield result = ObjectTransformer.deserialize(responseBody, Metafield.class);
        if (result != null) {
            testCase.setId(Integer.valueOf(result.getId()));
            testCase.setKey(result.getKey());
        }
        log("INFO: Created. JIRA Test item Id is: [" + testCase.getKey() + "].");
    }

    public void updateTestIssue(TestCase testCase) throws IOException{
        log("INFO: Updating JIRA Test item with Name: \"" + ("true".equals(config.getValue(ConfigProperty.CONSOLIDATE_PARAMETERIZED_TESTS)) ? testCase.getConsolidatedName() : testCase.getName()) + "\".");
        Issue issue = TestCaseToIssueTransformer.transform(config, testCase);

        HttpResponse response = HttpUtils.put(config, "api/2/issue/"+issue.getId(), issue);
        HttpUtils.ensureResponse(response, 204, "ERROR: Could not create JIRA Test item");
        log("INFO: Updated. JIRA Test item Id is: [" + testCase.getKey() + "].");
    }

    public void linkToStory(TestCase testCase) throws IOException {
        List<String> storyKeys = testCase.getStoryKeys();
        if (Boolean.valueOf(config.getValue(ConfigProperty.FORCE_STORY_LINK))) {
            if (storyKeys == null || storyKeys.isEmpty()) {
                throw new ZephyrSyncException("Linking Test issues to Story is mandatory, please check if Story marker exists in " + testCase.getKey());
            }
        }
        if (storyKeys == null) return;

        log("Linking Test issue " + testCase.getKey() + " to Stories " + testCase.getStoryKeys());
        for (String storyKey : storyKeys) {
            HttpResponse response = HttpUtils.post(config, "api/2/issueLink", createIssueLink(testCase, storyKey));
            HttpUtils.ensureResponse(response, 201, "Could not link Test issue: " + testCase.getId() + " to Story " + storyKey + ". " +
                    "Please check if Story issue exists and is valid");
        }
    }

    private IssueLink createIssueLink(TestCase testCase, String storyKey) {
        IssueLinkDirection direction = IssueLinkDirection.ofValue(config.getValue(ConfigProperty.LINK_DIRECTION));
        if (direction == IssueLinkDirection.inward) {
            return new IssueLink(testCase.getKey(), storyKey.toUpperCase(), config.getValue(ConfigProperty.LINK_TYPE));
        }
        return new IssueLink(storyKey.toUpperCase(), testCase.getKey(), config.getValue(ConfigProperty.LINK_TYPE));
    }

}
