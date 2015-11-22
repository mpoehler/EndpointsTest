package endpointstest;

import com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by marco on 11/17/15.
 */
public class LoginServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(LoginServlet.class);

    private UserService userService;;

    private CredentialStorage credentialStorage;

    private AuthConfiguration authConfiguration;

    private String targetUrl = "/app";

    @Override
    public void init() throws ServletException {
        super.init();

        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        userService = UserServiceFactory.getUserService();
        credentialStorage = (CredentialStorage) applicationContext.getBean("credentialStorage");
        authConfiguration = (AuthConfiguration) applicationContext.getBean("authConfiguration");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        log.info("login servlet called with " + req.getRequestURI());

        String callbackUrl = req.getRequestURL() + "?callback=true";

        GoogleClientSecrets clientSecrets;
        try {
            clientSecrets = new GoogleClientSecretsBuilder()
                    .forApi(GoogleClientSecretsBuilder.Api.ADWORDS)
                    .withClientSecrets(authConfiguration.getClientId(), authConfiguration.getClientSecret())
                    .build();
        } catch (ValidationException e) {
            throw new IOException("Problem on generation of GoogleClientSecrets", e);
        }

        GoogleAuthorizationCodeFlow authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                clientSecrets,
                authConfiguration.getScopes())
                .setApprovalPrompt("force")
                .setAccessType("offline").build();

        if (req.getParameter("callback") == null) {
            log.info("forward to oauth dialog");

            String uid = req.getParameter("uid");
            log.info("request credentials for uid " + uid);

            String authorizeUrl = authorizationFlow.newAuthorizationUrl().setState(uid).setRedirectUri(callbackUrl).build();

            log.info("authorizeUrl " + authorizeUrl);
            resp.sendRedirect(authorizeUrl);
            return;
        }

        log.info("received auth callback");
        String authorizationCode = req.getParameter("code");
        String uid = req.getParameter("state");
        GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationFlow.newTokenRequest(authorizationCode);
        tokenRequest.setRedirectUri(callbackUrl);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new JacksonFactory())
                .setClientSecrets(clientSecrets)
                .build();
        credential.setFromTokenResponse(tokenResponse);

        log.info("Credential-Refresh: " + credential.getRefreshToken());
        log.info("Credential-Access: " + credential.getAccessToken());
        log.info("Credential-ExpiresInSeconds: " + credential.getExpiresInSeconds());

        credentialStorage.save(uid, credential);

        log.info("redirect the user to the targetUrl");
        resp.sendRedirect(req.getRequestURL().substring(0,req.getRequestURL().indexOf("/", "https://".length())) + targetUrl);
    }
}
