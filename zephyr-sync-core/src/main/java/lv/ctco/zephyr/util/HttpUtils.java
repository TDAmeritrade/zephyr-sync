package lv.ctco.zephyr.util;

import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.HttpResponse;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.ZephyrSyncException;
import lv.ctco.zephyr.http.BasicAuthHttpProvider;
import lv.ctco.zephyr.http.HttpProvider;
import lv.ctco.zephyr.http.OAuthHttpProvider;
import lv.ctco.zephyr.oauth.JiraOAuthTokenFactory;
import lv.ctco.zephyr.enums.ConfigProperty;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class HttpUtils {

    private static HttpProvider getHttpProvider(Config config){
        if (config.getValue(ConfigProperty.OAUTH_TOKEN)!=null && !"".equals(config.getValue(ConfigProperty.OAUTH_TOKEN))){
            return new OAuthHttpProvider();
        }else{
            return new BasicAuthHttpProvider();
        }
    }

    private static OAuthParameters getOAuthParameters(Config config) throws InvalidKeySpecException, NoSuchAlgorithmException {
        JiraOAuthTokenFactory tokenFactory = new JiraOAuthTokenFactory(config);
        return tokenFactory.getJiraOAuthToken().createParameters();
    }

    public static String getAndReturnBody(Config config, String url) throws IOException {
        return getHttpProvider(config).getAndReturnBody(config, url);
    }

    public static HttpResponse post(Config config, String url, Object entity) throws IOException {
        return getHttpProvider(config).post(config, url, entity);
    }

    public static HttpResponse put(Config config, String url, Object entity) throws IOException {
        return getHttpProvider(config).put(config, url, entity);
    }

    public static void ensureResponse(HttpResponse response, int expectedStatusCode, String failureMessage) {
        if (response.getStatusCode() != expectedStatusCode) {
            String responseBody;
            try {
                responseBody = IOUtils.toString(response.getContent());
            } catch (IOException e) {
                Utils.log("Failed to parse response", e);
                responseBody = "<no response>";
            }
            throw new ZephyrSyncException(failureMessage + ": " + responseBody);
        }
    }

}