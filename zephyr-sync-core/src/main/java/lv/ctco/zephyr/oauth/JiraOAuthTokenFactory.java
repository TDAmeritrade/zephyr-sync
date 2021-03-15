package lv.ctco.zephyr.oauth;

import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.util.Base64;
import lv.ctco.zephyr.Config;
import lv.ctco.zephyr.enums.ConfigProperty;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class JiraOAuthTokenFactory {

    Config config;

    public JiraOAuthTokenFactory(Config config){
        this.config = config;
    }

    public JiraOAuthGetAccessToken getJiraOAuthToken() throws InvalidKeySpecException, NoSuchAlgorithmException {
        JiraOAuthGetAccessToken accessToken = new JiraOAuthGetAccessToken(config.getValue(ConfigProperty.JIRA_URL) + config.getValue(ConfigProperty.JIRA_ACCESS_TOKEN_ENDPOINT));
        accessToken.consumerKey = "OauthKey";
        accessToken.signer = getOAuthRsaSigner(config.getValue(ConfigProperty.OAUTH_PRIVATE_KEY));
        accessToken.transport = new ApacheHttpTransport();
        accessToken.verifier = config.getValue(ConfigProperty.OAUTH_SECRET);
        accessToken.temporaryToken = config.getValue(ConfigProperty.OAUTH_TOKEN);
        return accessToken;
    }


    /**
     * @param privateKey private key in PKCS8 format
     * @return OAuthRsaSigner
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private OAuthRsaSigner getOAuthRsaSigner(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = getPrivateKey(privateKey);
        return oAuthRsaSigner;
    }

    /**
     * Creates PrivateKey from string
     *
     * @param privateKey private key in PKCS8 format
     * @return private key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

}
