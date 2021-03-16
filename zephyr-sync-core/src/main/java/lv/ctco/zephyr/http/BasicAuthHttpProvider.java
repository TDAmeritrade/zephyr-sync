package lv.ctco.zephyr.http;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BasicAuthHttpProvider implements HttpProvider{

    private int getTimeout(Config config){
        try{
            return Integer.parseInt(config.getValue(ConfigProperty.HTTP_TIMEOUT));
        } catch (NumberFormatException e){
            return 60000;
        }
    }

    @Override
    public String getAndReturnBody(Config config, String url) throws IOException {
        ApacheHttpTransport transport = new ApacheHttpTransport();
        String uri = config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_REST_ENDPOINT) + url;
        Utils.log("GET: " + uri);
        HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(uri));
        request.setReadTimeout(getTimeout(config));

        setCommonHeaders(request);
        BasicAuthentication auth = new BasicAuthentication(config.getValue(ConfigProperty.USERNAME), config.getValue(ConfigProperty.PASSWORD));
        auth.intercept(request);
        HttpResponse response = request.execute();
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
        request.setReadTimeout(getTimeout(config));
        HttpResponse response = request.execute();
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
        request.setReadTimeout(getTimeout(config));
        HttpResponse response = request.execute();
        return response;
    }
    private static void setCommonHeaders(HttpRequest request) {
        request.getHeaders().set("Accept", "application/json");
    }

}
