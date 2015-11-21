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

        // check if the user is already logged in
        User user = userService.getCurrentUser();
        if (user == null) {
            // user is not logged in
            // send to login url and forward back to here
            log.info("forward user to application login");

            resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
            return;
        }

        log.info("user is logged in (server-side application login) with userId " + user.getEmail());

        log.info("check for valid access/refresh token (oauth for google services)");
        if (credentialStorage.get(user.getEmail())==null) {
            log.info("no token in credential storage");
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
                String authorizeUrl = authorizationFlow.newAuthorizationUrl().setRedirectUri(callbackUrl).build();

                log.info("authorizeUrl " + authorizeUrl);
                resp.sendRedirect(authorizeUrl);
                return;
            }

            log.info("received auth callback");
            String authorizationCode = req.getParameter("code");
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

            credentialStorage.save(user.getEmail(), credential);
        } else {
            log.info("credentials existing in database");
            Credential credential = credentialStorage.get(user.getEmail());

            // TODO check here that the credentials work!!!
            // - simple call to adwords?

            log.info("Credential-Refresh: " + credential.getRefreshToken());
            log.info("Credential-Access: " + credential.getAccessToken());
            log.info("Credential-ExpiresInSeconds: " + credential.getExpiresInSeconds());
        }

        log.info("redirect the user to the targetUrl");
        resp.sendRedirect(req.getRequestURL().substring(0,req.getRequestURL().indexOf("/", "https://".length())) + targetUrl);
    }
}
