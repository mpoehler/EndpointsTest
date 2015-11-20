package endpointstest;

import com.google.api.client.auth.oauth2.Credential;

/**
 * Created by marco on 11/19/15.
 */
public interface CredentialStorage {

    Credential get(String userId);
    void save(String userId, Credential credential);

}
