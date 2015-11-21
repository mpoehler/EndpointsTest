package endpointstest;

import com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by marco on 11/20/15.
 */
public class DatastoreCredentialStorageImpl implements CredentialStorage {

    private static Log log = LogFactory.getLog(DatastoreCredentialStorageImpl.class);

    private AuthConfiguration authConfiguration;

    private OfyService ofyService;

    @Override
    public Credential get(String userEmail) {

        CredentialEntity entity = ofyService.ofy().load().type(CredentialEntity.class).id(userEmail).now();
        if (entity == null) {
            return null;
        }

        GoogleClientSecrets clientSecrets = null;
        try {
            clientSecrets = new GoogleClientSecretsBuilder()
                    .forApi(GoogleClientSecretsBuilder.Api.ADWORDS)
                    .withClientSecrets(authConfiguration.getClientId(), authConfiguration.getClientSecret())
                    .build();
        } catch (ValidationException e) {
            log.error("Problem on generation of GoogleClientSecrets", e);
        }

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new JacksonFactory())
                .setClientSecrets(clientSecrets)
                .build();

        credential.setAccessToken(entity.getAccessToken());
        credential.setRefreshToken(entity.getRefreshToken());
        credential.setExpirationTimeMilliseconds(entity.getExpirationTimeMilliseconds());
        return credential;
    }

    @Override
    public void save(String userEmail, Credential credential) {
        log.info("save credential for userEmail: " + userEmail);
        ofyService.ofy().save().entity(new CredentialEntity(userEmail, credential)).now();
    }

    public void setOfyService(OfyService ofyService) {
        this.ofyService = ofyService;
    }

    public void setAuthConfiguration(AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }
}
