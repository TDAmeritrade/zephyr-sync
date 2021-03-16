package lv.ctco.zephyr.http;

import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.oauth.JiraOAuthTokenFactory;
import lv.ctco.zephyr.service.AuthService;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.util.Utils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class OAuthHttpProvider implements HttpProvider{

    @Override
    public String getAndReturnBody(Config config, String url) throws IOException {
        try {
            ApacheHttpTransport transport = new ApacheHttpTransport();
            HttpRequestFactory factory = transport.createRequestFactory(getOAuthParameters(config));
            GenericUrl genericUrl = new GenericUrl(config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url);
            HttpRequest request = factory.buildGetRequest(genericUrl);
            request.setReadTimeout(60000);
            com.google.api.client.http.HttpResponse response = request.execute();
            return Utils.readInputStream(response.getContent());
        }catch(Exception e){
            throw new IOException(e);
        }
    }

    @Override
    public HttpResponse post(Config config, String url, Object entity) throws IOException {
        try{
            String json = ObjectTransformer.serialize(entity);
            ApacheHttpTransport transport = new ApacheHttpTransport();
            HttpRequestFactory factory = transport.createRequestFactory(getOAuthParameters(config));
            GenericUrl genericUrl = new GenericUrl(config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url);
            HttpRequest request = factory.buildPostRequest(genericUrl, new ByteArrayContent("application/json", json.getBytes(StandardCharsets.UTF_8)));
            request.setReadTimeout(60000);
            HttpResponse response = request.execute();
            return response;
        }catch(Exception e){
            throw new IOException(e);
        }
    }

    @Override
    public HttpResponse put(Config config, String url, Object entity) throws IOException {
        try{
            String json = ObjectTransformer.serialize(entity);
            ApacheHttpTransport transport = new ApacheHttpTransport();
            HttpRequestFactory factory = transport.createRequestFactory(getOAuthParameters(config));
            GenericUrl genericUrl = new GenericUrl(config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url);
            HttpRequest request = factory.buildPutRequest(genericUrl, new ByteArrayContent("application/json", json.getBytes(StandardCharsets.UTF_8)));
            request.setReadTimeout(60000);
            HttpResponse response = request.execute();
            return response;
        }catch(Exception e){
            throw new IOException(e);
        }
    }

    private OAuthParameters getOAuthParameters(Config config) throws InvalidKeySpecException, NoSuchAlgorithmException {
        JiraOAuthTokenFactory tokenFactory = new JiraOAuthTokenFactory(config);
        return tokenFactory.getJiraOAuthToken().createParameters();
    }

}
