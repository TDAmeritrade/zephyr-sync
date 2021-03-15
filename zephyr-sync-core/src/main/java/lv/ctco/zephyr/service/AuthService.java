package lv.ctco.zephyr.service;

import com.google.api.client.http.HttpHeaders;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.beans.jira.ErrorResponse;
import lv.ctco.zephyr.beans.jira.Login;
import lv.ctco.zephyr.beans.jira.SessionResponse;
import lv.ctco.zephyr.util.HttpUtils;
import lv.ctco.zephyr.enums.ConfigProperty;
import lv.ctco.zephyr.util.ObjectTransformer;
import lv.ctco.zephyr.util.Utils;
import com.google.api.client.http.HttpResponse;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static lv.ctco.zephyr.util.Utils.log;

public class AuthService {
    public static final BasicCookieStore COOKIE = new BasicCookieStore();
    private static String jSessionId;

    private Config config;

    public AuthService(Config config) {
        this.config = config;
    }

    public void authenticateInJira() throws IOException {
        if (jSessionId == null) {
            Login login = new Login(config.getValue(ConfigProperty.USERNAME), config.getValue(ConfigProperty.PASSWORD));

            HttpResponse response = HttpUtils.post(config, "auth/1/session", login);
            if (response.getStatusCode() == 403) {
                HttpHeaders headers = response.getHeaders();
                if (response.getHeaders().containsKey("X-Authentication-Denied-Reason")) {
                    log("ERROR: JIRA authentication denied reason: " + response.getHeaders().get("X-Authentication-Denied-Reason").toString());
                }

                ErrorResponse errorResponse = ObjectTransformer.deserialize(Utils.readInputStream(response.getContent()), ErrorResponse.class);
                List<String> errorMessages = errorResponse.getErrorMessages();
                log("ERROR: JIRA authentication failed, error messages: " + errorMessages.stream().collect(Collectors.joining(", ")));
                return;
            }
            if (response.getStatusCode() != 200) {
                log("ERROR: JIRA authentication failed: " + response.getStatusCode() + " " + response.getStatusMessage());
                return;
            }
            SessionResponse loginResponse = ObjectTransformer.deserialize(Utils.readInputStream(response.getContent()), SessionResponse.class);
            if (loginResponse != null) {
                jSessionId = loginResponse.getSession().get("value");
            }
        }
    }

}