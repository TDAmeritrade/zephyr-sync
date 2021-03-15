package lv.ctco.zephyr.http;

import com.google.api.client.http.HttpResponse;
import lv.ctco.zephyr.Config;

import java.io.IOException;

public interface HttpProvider {

    public String getAndReturnBody(Config config, String url) throws IOException;

    public HttpResponse post(Config config, String url, Object entity) throws IOException;

    public HttpResponse put(Config config, String url, Object entity) throws IOException;
}
