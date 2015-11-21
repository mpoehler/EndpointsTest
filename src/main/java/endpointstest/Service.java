package endpointstest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
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

    @Autowired
    protected AdwordsService adwordsService;

    @ApiMethod(name = "warmup", path="warmup", httpMethod = ApiMethod.HttpMethod.GET)
    public void warmup() {
        // add warmup stuff here
    }

    @ApiMethod(name = "oauth.user", path="oauth.user")
    public User testLoginOauth(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return user;
    }

    @ApiMethod(name = "adwords.listaccounts", path="adwords.listaccounts")
    public List<AdwordsAccount> listAccounts(User user) throws OAuthRequestException, AdWordsException {
        log.info("call listAccounts() with user " + user.getEmail());
        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return adwordsService.listAccounts(user);
    }

    @ApiMethod(name = "adwords.listcachedaccounts", path="adwords.listcachedaccounts")
    public List<AdwordsAccount> listAccountsCached(User user) throws OAuthRequestException, AdWordsException {
        log.info("call listcachedaccounts() with user " + user.getEmail());
        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return adwordsService.listAccountsFromCache(user);
    }

}
