package endpointstest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

/**
 * Created by marco on 11/20/15.
 */
public class DatastoreCredentialStorageImpl implements CredentialStorage {

    private OfyService ofyService;

    @Override
    public Credential get(String userId) {

        CredentialEntity entity = ofyService.ofy().load().type(CredentialEntity.class).id(userId).now();
        if (entity == null) {
            return null;
        }

        GoogleCredential credential = new GoogleCredential();
        credential.setAccessToken(entity.getAccessToken());
        credential.setRefreshToken(entity.getRefreshToken());

        return credential;
    }

    @Override
    public void save(String userId, Credential credential) {
        CredentialEntity entity = new CredentialEntity(userId, credential.getAccessToken(), credential.getRefreshToken());
        ofyService.ofy().save().entity(entity).now();
    }

    public void setOfyService(OfyService ofyService) {
        this.ofyService = ofyService;
    }
}
