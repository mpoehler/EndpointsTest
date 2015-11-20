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
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by marco on 11/17/15.
 */
public class LoginServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(LoginServlet.class);

    private static String clientId = "894185615170-8f7h45jj25e310smph5do6lglnbtnggm.apps.googleusercontent.com";
    private static String clientSecret = "rl9HUYNm0vTOic3n-JnWwYR2";

    public static final String ADWORDS_API_SCOPE = "https://www.googleapis.com/auth/adwords";
    public static final String ANALYTICS_API_SCOPE = "https://www.googleapis.com/auth/analytics.readonly";
    public static final String SEARCHCONSOLE_API_SCOPE = "https://www.googleapis.com/auth/webmasters.readonly";
    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    private static final List<String> SCOPES = Lists.newArrayList(ADWORDS_API_SCOPE, ANALYTICS_API_SCOPE, EMAIL_SCOPE, SEARCHCONSOLE_API_SCOPE);

    private UserService userService;;

    private CredentialStorage credentialStorage;

    private AdwordsService adwordsService;

    private String targetUrl = "/";

    @Override
    public void init() throws ServletException {
        super.init();

        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        userService = UserServiceFactory.getUserService();

        credentialStorage = (CredentialStorage) applicationContext.getBean("credentialStorage");

        adwordsService = (AdwordsService) applicationContext.getBean("adwordsService");
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

        log.info("user is logged in");

        log.info("check for valid access/refresh token");
        if (credentialStorage.get(user.getUserId())==null || !isTokenValid(credentialStorage.get(user.getUserId()))) {
            log.info("no token or invalid token in credential storage");
            String callbackUrl = req.getRequestURL() + "?callback=true";

            GoogleClientSecrets clientSecrets;
            try {
                clientSecrets = new GoogleClientSecretsBuilder()
                        .forApi(GoogleClientSecretsBuilder.Api.ADWORDS)
                        .withClientSecrets(clientId, clientSecret)
                        .build();
            } catch (ValidationException e) {
                throw new IOException("Problem on generation of GoogleClientSecrets", e);
            }

            GoogleAuthorizationCodeFlow authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    new JacksonFactory(),
                    clientSecrets,
                    SCOPES)
                    .setApprovalPrompt("force")
                    .setAccessType("offline").build();

            if (req.getParameter("callback")==null) {
                log.info("forward to oauth dialog");
                String authorizeUrl = authorizationFlow.newAuthorizationUrl().setRedirectUri(callbackUrl).build();

                log.info("authorizeUrl " + authorizeUrl);
                resp.sendRedirect(authorizeUrl);
                return;
            }

            log.info("received auth callback");
            String authorizationCode = req.getParameter("code");
            GoogleAuthorizationCodeTokenRequest tokenRequest =
                    authorizationFlow.newTokenRequest(authorizationCode);
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

            credentialStorage.save(user.getUserId(), credential);
        } else {
            log.info("valid credentials existing in database");
            Credential credential = credentialStorage.get(user.getUserId());
            log.info("Credential-Refresh: " + credential.getRefreshToken());
            log.info("Credential-Access: " + credential.getAccessToken());
        }


        log.info("redirect the user to the targetUrl");
        resp.sendRedirect(req.getRequestURL().substring(0,req.getRequestURL().indexOf("/", "https://".length())) + targetUrl);
    }

    public boolean isTokenValid(Credential credential) {
        // check if the token is valid (expired?), is a refresh token set?
        // check if we can access the API with the given token
        return true;
    }

}
