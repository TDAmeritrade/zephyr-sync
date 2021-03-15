package lv.ctco.zephyr.mojo;

import lv.ctco.zephyr.ZephyrSyncService;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo( name = "sync" )
public class ZephyrSyncMojo
    extends AbstractMojo
    implements Config.Loader
{

    /**
     * User name used to connect to JIRA.
     */
    @Parameter( required = false )
    private String username;

    /**
     * Password for the user to connect to JIRA.
     */
    @Parameter( required = false )
    private String password;

    /**
     * Type of report that will be synchronized to Zephyr. One of `cucumber`, `allure`, `junit` or `nunit`.
     */
    @Parameter( required = true )
    private String reportType;

    /**
     * Key of project in JIRA.
     */
    @Parameter( required = true )
    private String projectKey;

    /**
     * FixVersion of a project to link Test results to.
     */
    @Parameter( required = false)
    private String releaseVersion;

    /**
     * Zephyr test cycle where the results will be linked to.
     */
    @Parameter( defaultValue = "Unknown")
    private String testCycle;

    /**
     * URL of JIRA, eg "http://your.jira.server/jira/".
     */
    @Parameter( required = true )
    private String jiraUrl;

    /**
     * URL of JIRA Rest Endpoint, eg "/rest/".
     */
    @Parameter( defaultValue = "/rest/")
    private String jiraRestEndpoint;

    /**
     * URL of JIRA Access Token Endpoint, eg "/plugins/servlet/oauth/access-token".
     */
    @Parameter( defaultValue = "/plugins/servlet/oauth/access-token")
    private String jiraAccessTokenEndpoint;

    /**
     * Path on the file system where reports are stored, eg "${project.build.directory}/cucumber-report/report.json".
     */
    @Parameter( required = true )
    private String reportPath;

    /**
     * RegexPath of the report file, eg "TEST.*\\.xml".
     */
    @Parameter( defaultValue = "TEST.*\\.xml")
    private String fileRegex;

    /**
     * If set to true, numerical prefix for test steps will be put (hierarchical).
     */
    @Parameter( defaultValue = "false" )
    private Boolean orderedSteps;

    /**
     * If set to true, sync will be failed in case at least one test doesn't have @Stories=ABC-XXX annotation.
     */
    @Parameter( defaultValue = "false" )
    private Boolean forceStoryLink;

    /**
     * 
     */
    @Parameter( defaultValue = "false" )
    private Boolean generateTestCaseUniqueId;

    /**
     * Name of JIRA attribute that stores 'Severity' attribute.
     */
    @Parameter
    private String severityAttribute;

    /**
     * Should new test cycle be created automatically?
     */
    @Parameter( defaultValue = "true" )
    private Boolean autoCreateTestCycle;

    /**
     * Specify an Assignee.
     */
    @Parameter
    private String assignee;

    /**
     * Link type between Test issue and related story (used in combination with `@Stories` annotation).
     */
    @Parameter
    private String linkType;

    /**
     * Link direction between Test issue and related story (one of: inward, outward)
     */
    @Parameter
    private String linkDirection;

    /**
     * Use current git branch for Cycle
     */
    @Parameter( defaultValue = "true" )
    private Boolean useGitBranchForCycle;

    /**
     * Fail the build if there are any test failures
     */
    @Parameter( defaultValue = "true")
    private Boolean failBuildOnTestFailure;

    /**
     * Update the Jira Issue if it already exists
     */
    @Parameter( defaultValue = "false" )
    private Boolean updateIssue;

    /**
     * Regex to search for test description
     */
    @Parameter
    private String descriptionRegex;

    /**
     * Regex match group to use for test description
     */
    @Parameter
    private String descriptionRegexMatchGroup;

    /**
     * Application name.  Used to prefix git branch when using git branch for cycle.
     */
    @Parameter
    private String applicationName;

    /**
     * Consolidates Junit5 parameterized tests into one Jira Test.
     */
    @Parameter( defaultValue = "true")
    private Boolean consolidateParameterizedTests;

    /**
     * The generated token for OAuth
     */
    @Parameter
    private String oauthToken;

    /**
     * The client secret generated with OAuth token
     */
    @Parameter
    private String oauthSecret;

    /**
     * The private key for OAuth authentication
     */
    @Parameter
    private String oauthPrivateKey;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Config config = new Config( this );

        ZephyrSyncService syncService = new ZephyrSyncService( config );
        try
        {
            syncService.execute();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot sync test results into zephyr", e );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    public void execute( Config config )
    {
        config.setValue( ConfigProperty.USERNAME, username );
        config.setValue( ConfigProperty.PASSWORD, password );
        config.setValue( ConfigProperty.REPORT_TYPE, reportType );
        config.setValue( ConfigProperty.PROJECT_KEY, projectKey );
        config.setValue( ConfigProperty.RELEASE_VERSION, releaseVersion );
        config.setValue( ConfigProperty.JIRA_URL, jiraUrl );
        config.setValue( ConfigProperty.JIRA_REST_ENDPOINT, jiraRestEndpoint);
        config.setValue( ConfigProperty.JIRA_ACCESS_TOKEN_ENDPOINT, jiraAccessTokenEndpoint);
        config.setValue( ConfigProperty.REPORT_PATH, reportPath );
        config.setValue( ConfigProperty.FILE_REGEX, fileRegex);
        config.setValue( ConfigProperty.ORDERED_STEPS, orderedSteps );
        config.setValue( ConfigProperty.FORCE_STORY_LINK, forceStoryLink );
        config.setValue( ConfigProperty.GENERATE_TEST_CASE_UNIQUE_ID, generateTestCaseUniqueId );
        config.setValue( ConfigProperty.SEVERITY, severityAttribute );
        config.setValue( ConfigProperty.AUTO_CREATE_TEST_CYCLE, autoCreateTestCycle );
        config.setValue( ConfigProperty.ASSIGNEE, assignee );
        config.setValue( ConfigProperty.LINK_TYPE, linkType );
        config.setValue( ConfigProperty.LINK_DIRECTION, linkDirection);
        config.setValue( ConfigProperty.USE_GIT_BRANCH_FOR_CYCLE, useGitBranchForCycle);
        config.setValue( ConfigProperty.FAIL_BUILD_ON_TEST_FAILURE, failBuildOnTestFailure);
        config.setValue( ConfigProperty.UPDATE_ISSUE, updateIssue);
        config.setValue( ConfigProperty.DESCRIPTION_REGEX, descriptionRegex);
        config.setValue( ConfigProperty.DESCRIPTION_REGEX_MATCH_GROUP, descriptionRegexMatchGroup);
        config.setValue( ConfigProperty.APPLICATION_NAME, applicationName);
        config.setValue( ConfigProperty.CONSOLIDATE_PARAMETERIZED_TESTS, consolidateParameterizedTests);
        config.setValue( ConfigProperty.OAUTH_TOKEN, oauthToken);
        config.setValue( ConfigProperty.OAUTH_SECRET, oauthSecret);
        config.setValue( ConfigProperty.OAUTH_PRIVATE_KEY, oauthPrivateKey);
        if (useGitBranchForCycle != null && useGitBranchForCycle){
            String branch = (applicationName != null && !"".equals(applicationName) ? applicationName + "-" : "") + getCurrentGitBranch();
            System.out.println("##### Using branch as cycle: " + branch);
            config.setValue(ConfigProperty.TEST_CYCLE, branch);
        }else {
            System.out.println("##### Using property as cycle: " + testCycle);
            config.setValue(ConfigProperty.TEST_CYCLE, testCycle);
        }
    }

    public static String getCurrentGitBranch(){
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
            process.waitFor();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            return reader.readLine();
        }catch(Exception e){
            System.out.println("##### Could not determine git branch: " + e.getMessage());
            return "Unknown";
        }
    }
}
