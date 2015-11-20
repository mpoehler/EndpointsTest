package endpointstest;

import com.google.api.client.auth.oauth2.Credential;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 11/19/15.
 */
public class MemoryCredentialStorage implements CredentialStorage {

    private Map<String, Credential> storage = new HashMap<>();

    @Override
    public Credential get(String userId) {
        return storage.get(userId);
    }

    @Override
    public void save(String userId, Credential credential) {
        storage.put(userId, credential);
    }
}
