package nl.authentication.management.app.data.login;

import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private UUID userId;
    private String accessToken;
    private Long expiresAt;
    private String refreshToken;
    private String displayName;
    private Boolean active;
    private String username;

    public LoggedInUser() {}

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.forLanguageTag("nl_NL"),
                "username=%s, userId=%s, displayName=%s, active=%s, expiresAt=%d, refreshToken=%s, accessToken=%s",
                username, userId, displayName, active, expiresAt, refreshToken, accessToken);
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
