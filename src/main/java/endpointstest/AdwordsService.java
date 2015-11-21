package endpointstest;

import com.google.api.ads.adwords.jaxws.factory.AdWordsServices;
import com.google.api.ads.adwords.jaxws.utils.v201506.SelectorBuilder;
import com.google.api.ads.adwords.jaxws.v201506.cm.Selector;
import com.google.api.ads.adwords.jaxws.v201506.mcm.*;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 11/20/15.
 */
public class AdwordsService {

    public static Log log = LogFactory.getLog(AdwordsService.class);

    private CredentialStorage credentialStorage;

    private AuthConfiguration authConfiguration;

    private Map<String, AdWordsSession> sessionMap = new HashMap<String, AdWordsSession>();

    private AdWordsServices adWordsServices = new AdWordsServices();

    private OfyService ofyService;

    /**
     * This is a try to cache the adwords session
     *
     * @param accountId
     * @return
     */
    public AdWordsSession getSession(String accountId, User user) throws AdWordsException {
        String key = user.getUserId() + "-" + accountId;
        if (!sessionMap.containsKey(key)) {
            sessionMap.put(key, createSession(accountId, user));
        }
        AdWordsSession adWordsSession = sessionMap.get(key);
        return adWordsSession;
    }

    /**
     * creates a session for the given user and account
     * @param accountId the adwords accountId, maybe null for non account specific access
     * @param user the user
     * @return a valid AdWordSession, or null if the session can
     */
    public AdWordsSession createSession(String accountId, User user) throws AdWordsException {

        // lookup the credentials
        Credential credential = credentialStorage.get(user.getEmail());
        if (credential == null) {
            throw new AdWordsException("Credentials not found for email " + user.getEmail());
        }

        log.info("loaded credentials from datastore: " + credential);
        try {
            // TODO here we can add a check for the lifetime of the credential
            // Does the refreshToken work when it comes directly from the datastore?
            boolean success = credential.refreshToken();
            if (!success)
                throw new AdWordsException("Problem on Token refresh.");
        } catch (IOException ioe) {
            throw new AdWordsException("Can't refresh Token", ioe);
        }

        // create the session
        try {
            Configuration config = new MapConfiguration(new HashMap());
            config.addProperty("api.adwords.clientId", authConfiguration.getClientId());
            config.addProperty("api.adwords.clientSecret", authConfiguration.getClientSecret());
            config.addProperty("api.adwords.developerToken", authConfiguration.getDeveloperToken());
            config.addProperty("api.adwords.userAgent", authConfiguration.getUserAgent());

            if (accountId == null) {
                return new AdWordsSession.Builder()
                        .withDeveloperToken(config.getProperty("api.adwords.developerToken") + "")
                        .withOAuth2Credential(credential)
                        .withUserAgent(config.getProperty("api.adwords.userAgent") + "")
                        .build();
            } else {
                return new AdWordsSession.Builder()
                        .withDeveloperToken(config.getProperty("api.adwords.developerToken") + "")
                        .withOAuth2Credential(credential)
                        .withUserAgent(config.getProperty("api.adwords.userAgent") + "")
                        .withClientCustomerId(accountId)
                        .build();
            }
        } catch (ValidationException ve) {
            throw new AdWordsException("Problem on building AdWordsSession", ve);
        }
    }

    public List<AdwordsAccount> listAccountsFromCache(User user) {
        log.info("check cache for account for " + user.getEmail());
        AdwordsAccountCacheEntry adwordsAccountCacheEntry = ofyService.ofy().load().type(AdwordsAccountCacheEntry.class).id(user.getEmail()).now();
        if (adwordsAccountCacheEntry != null) {
            log.info("cache hit");
            return adwordsAccountCacheEntry.getAccounts();
        }
        log.info("cache miss");
        List<AdwordsAccountCacheEntry> adwordsAccountCacheEntries =  ofyService.ofy().load().type(AdwordsAccountCacheEntry.class).list();
        log.info("found " + adwordsAccountCacheEntries.size() + " entries");
        return null;
    }

    /**
     * lists all Accounts for the given userId
     */
    public List<AdwordsAccount> listAccounts(User user) throws AdWordsException {

        log.info("call list Accounts for user " + user);
        List<AdwordsAccount> result = new ArrayList<AdwordsAccount>();

        AdWordsSession session = getSession(null, user);
        log.info("AdwordsSession without clientCustomerId created");

        CustomerServiceInterface customerService = adWordsServices.get(session, CustomerServiceInterface.class);
        log.info("customerService created");

        Customer customer = null;
        try {
            customer = customerService.get();
            log.info("customerService.get() done");

            if (customer.isCanManageClients()) {
                // customer is MCC Account

                // construct new Adwords Session WITH customerId
                // session = new AdWordsSession.Builder().withOAuth2Credential(credential).from(config).withClientCustomerId("" + customer.getCustomerId()).build();
                session = getSession("" + customer.getCustomerId(), user);

                log.info("Adwords session with clientCustomerId created");

                ManagedCustomerServiceInterface managedCustomerService = adWordsServices.get(session, ManagedCustomerServiceInterface.class);
                log.info("managedCustomerService created");

                Selector selector = new SelectorBuilder().fields("CustomerId", "Name", "CompanyName", "CanManageClients", "CurrencyCode", "DateTimeZone").build();
                ManagedCustomerPage page = managedCustomerService.get(selector);

                log.info("call to managedCustomerService");

                // Display serviced account graph.
                if (page.getEntries() != null) {
                    // Create map from customerId to customer node.
                    Map<Long, ManagedCustomerTreeNode> customerIdToCustomerNode =
                            new HashMap<Long, ManagedCustomerTreeNode>();

                    // Create account tree nodes for each customer.
                    for (ManagedCustomer c : page.getEntries()) {
                        ManagedCustomerTreeNode node = new ManagedCustomerTreeNode();
                        node.account = c;
                        customerIdToCustomerNode.put(c.getCustomerId(), node);
                    }

                    // For each link, connect nodes in tree.
                    if (page.getLinks() != null) {
                        for (ManagedCustomerLink link : page.getLinks()) {
                            ManagedCustomerTreeNode managerNode = customerIdToCustomerNode.get(
                                    link.getManagerCustomerId());
                            ManagedCustomerTreeNode childNode = customerIdToCustomerNode.get(
                                    link.getClientCustomerId());
                            childNode.parentNode = managerNode;
                            if (managerNode != null) {
                                managerNode.childAccounts.add(childNode);
                            }
                        }
                    }

                    // Find the root account node in the tree.
                    ManagedCustomerTreeNode rootNode = null;
                    for (ManagedCustomer account : page.getEntries()) {
                        if (customerIdToCustomerNode.get(account.getCustomerId()).parentNode == null) {
                            rootNode = customerIdToCustomerNode.get(account.getCustomerId());
                            break;
                        }
                    }

                    // Display account tree.
                    log.info("CustomerId, Name");
                    log.info(rootNode.toTreeString(0, new StringBuffer()));

                    result = rootNode.toAdwordsAccountList(result);
                }
            } else {
                // customer is single Account
                log.info(customer.getCustomerId() + " " + customer.getDescriptiveName() + " " + customer.getCompanyName() + " " + customer.getCurrencyCode() + " " + customer.getDateTimeZone());
                result.add(new AdwordsAccount(customer.getDescriptiveName(), customer.getCustomerId() + ""));
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }

        ofyService.ofy().save().entity(new AdwordsAccountCacheEntry(user.getEmail(), result)).now();

        return result;
    }

    /**
     * Example implementation of a node that would exist in an account tree.
     */
    private static class ManagedCustomerTreeNode {
        protected ManagedCustomerTreeNode parentNode;
        protected ManagedCustomer account;
        protected List<ManagedCustomerTreeNode> childAccounts = new ArrayList<ManagedCustomerTreeNode>();

        /**
         * Default constructor.
         */
        public ManagedCustomerTreeNode() {}

        @Override
        public String toString() {
            return String.format("%s, %s", new Object[]{account.getCustomerId(), account.getName()});
        }

        /**
         * Returns a string representation of the current level of the tree and
         * recursively returns the string representation of the levels below it.
         *
         * @param depth the depth of the node
         * @param sb the string buffer containing the tree representation
         * @return the tree string representation
         */
        public StringBuffer toTreeString(int depth, StringBuffer sb) {
            sb.append(StringUtils.repeat("-", depth * 2)).append(this).append("\n");
            for (ManagedCustomerTreeNode childAccount : childAccounts) {
                childAccount.toTreeString(depth + 1, sb);
            }
            return sb;
        }

        public List<AdwordsAccount> toAdwordsAccountList(List<AdwordsAccount> list) {

            // build parent MCC Names
            ManagedCustomerTreeNode parentNode = this.parentNode;
            StringBuffer strbuf = new StringBuffer();
            while (parentNode != null) {

                String name = parentNode.account.getName();
                if (name==null || "".equals(name.trim())) {
                    name = parentNode.account.getCompanyName();
                }
                strbuf.insert(0, name + " > " );
                parentNode = parentNode.parentNode;
            }

            if (this.account.isCanManageClients()) {
                // and add all his children
                for (ManagedCustomerTreeNode childNode : childAccounts) {
                    list = childNode.toAdwordsAccountList(list);
                }
            } else {
                // add this AdwordsAccount
                String name = this.account.getName();
                if (name==null || "".equals(name.trim())) {
                    name = this.account.getCompanyName();
                }
                list.add(new AdwordsAccount(strbuf.toString() + name, this.account.getCustomerId() + "" ));
            }
            return list;
        }
    }

    public void setCredentialStorage(CredentialStorage credentialStorage) {
        this.credentialStorage = credentialStorage;
    }

    public void setAuthConfiguration(AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    public void setOfyService(OfyService ofyService) {
        this.ofyService = ofyService;
    }
}
