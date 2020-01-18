package nl.authentication.management.app.data;

import android.content.SharedPreferences;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.NonNull;

/**
 * {@link SharedPreferences} wrapper class for oauth information
 */
public class OAuthSharedPreferences {
    private SharedPreferences sharedPrefs;

    private static class Keys {
        private static final String ACCESS_TOKEN = "accessToken";
        private static final String EXPIRES_AT = "expiresAt";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final String DISPLAY_NAME = "displayName";
        private static final String USER_ID = "userId";
        private static final String ACTIVE = "active";
        private static final String USERNAME = "username";
    }

    @Inject
    public OAuthSharedPreferences(@Named("auth") SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    public void setAccessToken(@NonNull String accessToken) {
        sharedPrefs.edit().putString(Keys.ACCESS_TOKEN, accessToken).apply();
    }

    public void setRefreshToken(@NonNull String refreshToken) {
        sharedPrefs.edit().putString(Keys.REFRESH_TOKEN, refreshToken).apply();
    }

    public void setUserId(@NonNull UUID userId) {
        sharedPrefs.edit().putString(Keys.USER_ID, userId.toString()).apply();
    }

    public void setDisplayName(@NonNull String displayName) {
        sharedPrefs.edit().putString(Keys.DISPLAY_NAME, displayName).apply();
    }

    public void setActive(@NonNull Boolean active) {
        sharedPrefs.edit().putBoolean(Keys.ACTIVE, active).apply();
    }

    public void setExpiresAt(@NonNull Long expiresAt) {
        sharedPrefs.edit().putLong(Keys.EXPIRES_AT, expiresAt).apply();
    }

    public void setUsername(@NonNull String username) {
        sharedPrefs.edit().putString(Keys.USERNAME, username).apply();
    }

    @NonNull
    public String getAccessToken() {
        return sharedPrefs.getString(Keys.ACCESS_TOKEN, null);
    }

    @NonNull
    public String getRefreshToken() {
        return sharedPrefs.getString(Keys.REFRESH_TOKEN, null);
    }

    @NonNull
    public UUID getUserId() {
        return UUID.fromString(sharedPrefs.getString(Keys.USER_ID, null));
    }

    @NonNull
    public String getDisplayName() {
        return sharedPrefs.getString(Keys.DISPLAY_NAME, null);
    }

    @NonNull
    public Boolean getActive() {
        if (sharedPrefs.contains(Keys.ACTIVE)) {
            return sharedPrefs.getBoolean(Keys.ACTIVE, false);
        }
        throw new NullPointerException("active is null");
    }

    @NonNull
    public Long getExpiresAt() {
        if (sharedPrefs.contains(Keys.EXPIRES_AT)) {
            return sharedPrefs.getLong(Keys.EXPIRES_AT, 0L);
        }
        throw new NullPointerException("expiresAt is null");
    }

    @NonNull
    public String getUsername() {
        return sharedPrefs.getString(Keys.USERNAME, null);
    }

    public void wipeUserLoginInfo() {
        sharedPrefs.edit()
                .clear()
                .apply();
    }

}
