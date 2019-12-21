package nl.authentication.management.app.api;

import androidx.annotation.NonNull;

public class Tokens {
    private String accessToken;
    private Long expiresAt;
    private String refreshToken;

    public Tokens(@NonNull String accessToken, @NonNull Long expiresAt, @NonNull String refreshToken) {
        setAccessToken(accessToken);
        setExpiresAt(expiresAt);
        setRefreshToken(refreshToken);
    }

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

    private void setAccessToken(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken;
    }

    private void setRefreshToken(@NonNull String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @NonNull
    public Long getExpiresAt() {
        return expiresAt;
    }

    private void setExpiresAt(@NonNull Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
