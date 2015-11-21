package endpointstest;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by marco on 21.11.15.
 */
@Entity
@Cache
public class AdwordsAccountCacheEntry {

    @Id
    private String userId;

    private List<AdwordsAccount> accounts;

    public AdwordsAccountCacheEntry() {
    }

    public AdwordsAccountCacheEntry(String userId, List<AdwordsAccount> accounts) {
        this.userId = userId;
        this.accounts = accounts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<AdwordsAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AdwordsAccount> accounts) {
        this.accounts = accounts;
    }
}
