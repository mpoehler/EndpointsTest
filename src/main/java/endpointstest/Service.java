package endpointstest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.List;

/**
 * Created by marco on 11/17/15.
 */
@Api(
        name = "endpointstest",
        version = "v1",
        scopes = {"https://www.googleapis.com/auth/userinfo.email"},
        clientIds = {"894185615170-8f7h45jj25e310smph5do6lglnbtnggm.apps.googleusercontent.com"}
)
public class Service {

    public static Log log = LogFactory.getLog(Service.class);

    public Service() {
        // class instantiated by google endpoints api, we need to force spring processing
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public class BooleanServiceResult {
        String userId;
        boolean result;
        String message;

        BooleanServiceResult(String userId, boolean result) {
            this.userId = userId;
            this.result = result;
        }

        BooleanServiceResult(String userId, boolean result, String message) {
            this.userId = userId;
            this.result = result;
            this.message = message;
        }

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    @Autowired
    protected AdwordsService adwordsService;

    @Autowired
    protected CredentialStorage credentialStorage;

    @ApiMethod(name = "warmup", path="warmup", httpMethod = ApiMethod.HttpMethod.GET)
    public void warmup() {
        log.info("Call warmup");
        // add warmup stuff here
    }

    @ApiMethod(name = "checkcredentials", path="checkcredentials")
    public BooleanServiceResult checkcredentials(User user) throws OAuthRequestException {
        log.info("call checkcredentials() with user " + user.getUserId());

        String userId = user.getUserId();

        // if we are on development, override userId from parameter
        //if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
        //    userId = uid;
        //}

        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        if (credentialStorage.get(userId) != null) {
            return new BooleanServiceResult(user.getUserId(), true);
        }
        return new BooleanServiceResult(user.getUserId(), false,"No Credentials");
    }

    @ApiMethod(name = "adwords.listaccounts", path="adwords.listaccounts")
    public List<AdwordsAccount> listAccounts(User user) throws OAuthRequestException, AdWordsException {
        log.info("call listAccounts() with user " + user.getUserId());

        String userId = user.getUserId();

        // if we are on development, override userId from parameter
        //if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
        //    userId = uid;
        //}

        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return adwordsService.listAccounts(userId);
    }

    @ApiMethod(name = "adwords.listcachedaccounts", path="adwords.listcachedaccounts")
    public List<AdwordsAccount> listAccountsCached(User user) throws OAuthRequestException, AdWordsException {
        log.info("call listcachedaccounts() with user " + user.getUserId());

        String userId = user.getUserId();

        // if we are on development, override userId from parameter
        //if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
        //    userId = uid;
        //}

        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return adwordsService.listAccountsFromCache(userId);
    }

}
