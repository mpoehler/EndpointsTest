package endpointstest;

import com.google.api.client.auth.oauth2.Credential;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by marco on 11/20/15.
 */
@Entity
public class CredentialEntity {

    @Id
    private String userId;

    private String accessToken;

    private String refreshToken;

    public CredentialEntity() {
    }

    public CredentialEntity(String userId, Credential credential) {
        this.userId = userId;
        this.accessToken = credential.getAccessToken();
        this.refreshToken = credential.getRefreshToken();
    }

    public CredentialEntity(String userId, String accessToken, String refreshToken) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "CredentialEntity{" +
                "userId='" + userId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
