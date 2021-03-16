package lv.ctco.zephyr.http;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BasicAuthHttpProvider implements HttpProvider{

    @Override
    public String getAndReturnBody(Config config, String url) throws IOException {
        ApacheHttpTransport transport = new ApacheHttpTransport();
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("GET: " + uri);
        HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(uri));
        request.setReadTimeout(60000);

        setCommonHeaders(request);
        BasicAuthentication auth = new BasicAuthentication(config.getValue(ConfigProperty.USERNAME), config.getValue(ConfigProperty.PASSWORD));
        auth.intercept(request);
        HttpResponse response = request.execute();
        //httpClient.close();
        return Utils.readInputStream(response.getContent());
    }

    @Override
    public HttpResponse post(Config config, String url, Object entity) throws IOException {
        String json = ObjectTransformer.serialize(entity);
        ApacheHttpTransport transport = new ApacheHttpTransport();
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("POST: " + uri);
        HttpRequest request = transport.createRequestFactory().buildPostRequest(new GenericUrl(uri), new ByteArrayContent("application/json", json.getBytes(StandardCharsets.UTF_8)));
        setCommonHeaders(request);
        BasicAuthentication auth = new BasicAuthentication(config.getValue(ConfigProperty.USERNAME), config.getValue(ConfigProperty.PASSWORD));
        auth.intercept(request);
        request.setReadTimeout(60000);
        HttpResponse response = request.execute();
        //httpClient.close();
        return response;
    }

    @Override
    public HttpResponse put(Config config, String url, Object entity) throws IOException {
        String json = ObjectTransformer.serialize(entity);
        ApacheHttpTransport transport = new ApacheHttpTransport();
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("PUT: " + uri);
        HttpRequest request = transport.createRequestFactory().buildPutRequest(new GenericUrl(uri), new ByteArrayContent("application/json", json.getBytes(StandardCharsets.UTF_8)));
        setCommonHeaders(request);
        BasicAuthentication auth = new BasicAuthentication(config.getValue(ConfigProperty.USERNAME), config.getValue(ConfigProperty.PASSWORD));
        auth.intercept(request);
        request.setReadTimeout(60000);
        HttpResponse response = request.execute();
        //httpClient.close();
        return response;
    }
    private static void setCommonHeaders(HttpRequest request) throws IOException {
        request.getHeaders().set("Accept", "application/json");
    }

}
