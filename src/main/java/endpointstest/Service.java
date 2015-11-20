package endpointstest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public Service() {
        // class instantiated by google endpoints api, we need to force spring processing
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Autowired
    @Qualifier("ofyService")
    protected AdwordsService adwordsService;

    @ApiMethod(name = "oauth.user", path="oauth.user", httpMethod = ApiMethod.HttpMethod.GET)
    public User testLoginOauth(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return user;
    }

    @ApiMethod(name = "adwords.listaccounts", path="adwords.listaccounts", httpMethod = ApiMethod.HttpMethod.GET)
    public List<AdwordsAccount> list(User user) throws OAuthRequestException, AdWordsException {
        if (user == null) {
            throw new OAuthRequestException("Invalid user");
        }
        return adwordsService.listAccounts(user.getUserId());
    }

/*
    @ApiMethod(name = "adwords.listsomething", path="adwords.listsomething", httpMethod = ApiMethod.HttpMethod.GET)
    public List<String> listSomething() throws OAuthRequestException, AdWordsException {
        return Arrays.asList(new String[] {"huch", "Hoppla"});
    }

    @ApiMethod(name = "adwords.showuser", path="adwords.showuser" , httpMethod = ApiMethod.HttpMethod.GET)
    public User showUser() throws OAuthRequestException, AdWordsException {
        return UserServiceFactory.getUserService().getCurrentUser();
    }

    protected void setAdwordsService(AdwordsService adwordsService) {
        this.adwordsService = adwordsService;
    }
*/
}
