package lv.ctco.zephyr.http;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.service.AuthService;
import lv.ctco.zephyr.util.Utils;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

public class CookieHttpProvider implements HttpProvider{

    private static CloseableHttpClient getHttpClient() {
        return HttpClientBuilder
                .create()
                .setDefaultCookieStore(AuthService.COOKIE)
                .build();
    }

    @Override
    public String getAndReturnBody(Config config, String url) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        ApacheHttpTransport transport = new ApacheHttpTransport(httpClient);
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("GET: " + uri);
        HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(uri));
        setCommonHeaders(request);
        HttpResponse response = request.execute();
        httpClient.close();
        return Utils.readInputStream(response.getContent());
    }

    @Override
    public HttpResponse post(Config config, String url, Object entity) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        ApacheHttpTransport transport = new ApacheHttpTransport(httpClient);
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("POST: " + uri);
        HttpContent content = new JsonHttpContent( new GsonFactory(), entity);
        System.out.println("########## content: " + content.toString());
        HttpRequest request = transport.createRequestFactory().buildPostRequest(new GenericUrl(uri), content);
        setCommonHeaders(request);
        HttpResponse response = request.execute();
        httpClient.close();
        return response;
    }

    @Override
    public HttpResponse put(Config config, String url, Object entity) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        ApacheHttpTransport transport = new ApacheHttpTransport(httpClient);
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("PUT: " + uri);
        HttpContent content = new JsonHttpContent( new GsonFactory(), entity);
        HttpRequest request = transport.createRequestFactory().buildPutRequest(new GenericUrl(uri), content);
        setCommonHeaders(request);
        HttpResponse response = request.execute();
        httpClient.close();
        return response;
    }
    private static void setCommonHeaders(HttpRequest request) throws IOException {
        request.getHeaders().set("Accept", "application/json");
    }

}
