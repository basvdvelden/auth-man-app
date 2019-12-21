package nl.authentication.management.app.data.login;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import nl.authentication.management.app.api.AuthNotifier;
import nl.authentication.management.app.api.Tokens;
import nl.authentication.management.app.data.AuthCache;
import nl.authentication.management.app.data.OAuthSharedPreferences;
import nl.authentication.management.app.data.Result;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
@Singleton
public class LoginRepository {
    private static final String TAG = "LoginRepository";

    // data sources
    private final LoginDataSource dataSource;
    private final OAuthSharedPreferences sharedPrefs;
    private final AuthCache authCache;

    private final AuthNotifier authNotifier;
    private BehaviorSubject<Boolean> isLoggedIn = BehaviorSubject.create();

    @Inject
    public LoginRepository(LoginDataSource dataSource, OAuthSharedPreferences sharedPrefs, AuthCache authCache,
                    AuthNotifier authNotifier) {
        this.dataSource = dataSource;
        this.sharedPrefs = sharedPrefs;
        this.authCache = authCache;
        this.authNotifier = authNotifier;

        this.authNotifier.getNeedToLogout().subscribe(needToLogout -> {
            if (needToLogout) {
                boolean alreadyPublished = false;
                if (this.getCurrentUser() != null) {
                    this.logout();
                    alreadyPublished = true;
                }
                Log.d(TAG, "logged out");
                if (!alreadyPublished) {
                    isLoggedIn.onNext(false);
                }
            }
        });
        this.authNotifier.getTokens().subscribe(tokens -> {
            Log.d(TAG, String.format("saving tokens received from refresh, rt=%s, at=%s",
                    tokens.getRefreshToken(), tokens.getAccessToken()));
            saveTokens(tokens.getAccessToken(), tokens.getExpiresAt(), tokens.getRefreshToken());
        });

        if (getCurrentUser() == null) {
            Log.d(TAG, "logged out");
            isLoggedIn.onNext(false);
        } else {
            Log.d(TAG, String.format("logged in, user=%s", getCurrentUser()));
            isLoggedIn.onNext(true);
            this.authNotifier.setAuthInfo(getCurrentUser().getUserId(),
                    new Tokens(getAccessToken(), getExpiresAt(), getRefreshToken())
            );
        }
    }

    public Subject<Boolean> getIsLoggedIn() {
        return this.isLoggedIn;
    }

    public LoggedInUser getCurrentUser() {
        LoggedInUser user = authCache.getLoggedInUser();
        if (user == null) {
            user = fetchUserFromPrefs();
            authCache.setLoggedInUser(user);
        }
        return user;
    }

    public String getRefreshToken() {
        String refreshToken = authCache.getLoggedInUser().getRefreshToken();
        if (refreshToken == null) {
            refreshToken = sharedPrefs.getRefreshToken();
        }
        return refreshToken;
    }

    public String getAccessToken() {
        String accessToken = authCache.getLoggedInUser().getAccessToken();
        if (accessToken == null) {
            accessToken = sharedPrefs.getAccessToken();
        }
        return accessToken;
    }

    public Long getExpiresAt() {
        Long expiresAt = authCache.getLoggedInUser().getExpiresAt();
        if (expiresAt == null) {
            expiresAt = sharedPrefs.getExpiresAt();
        }
        return expiresAt;
    }

    public void saveTokens(String accessToken, Long expiresAt, String refreshToken) {
        authCache.getLoggedInUser().setAccessToken(accessToken);
        authCache.getLoggedInUser().setExpiresAt(expiresAt);
        authCache.getLoggedInUser().setRefreshToken(refreshToken);
        sharedPrefs.setAccessToken(accessToken);
        sharedPrefs.setExpiresAt(expiresAt);
        sharedPrefs.setRefreshToken(refreshToken);
    }

    public void logout() {
        UUID uuid = getCurrentUser().getUserId();
        authCache.setLoggedInUser(null);
        sharedPrefs.wipeUserLoginInfo();
        isLoggedIn.onNext(false);
        dataSource.logout(uuid);
    }

    public Result<LoggedInUser> login(String username, String password) {
        Result<LoggedInUser> result = dataSource.login(username, password);
        handleLoginResult(result);
        return result;
    }

    public Result<LoggedInUser> login(GoogleSignInAccount googleSignInAccount) {
        Result<LoggedInUser> result = dataSource.login(googleSignInAccount);
        handleLoginResult(result);
        return result;
    }

    private void handleLoginResult(Result<LoggedInUser> result) {
        if (result instanceof Result.Success) {
            LoggedInUser user = ((Result.Success<LoggedInUser>) result).getData();
            Log.d(TAG, String.format("logged in, user=%s", user));
            setLoggedInUser(user);

            // publish tokens to auth interceptor
            this.authNotifier.setAuthInfo(user.getUserId(),
                    new Tokens(user.getAccessToken(), user.getExpiresAt(), user.getRefreshToken()));

            // notify observers of login
            this.isLoggedIn.onNext(true);
        }
    }

    private LoggedInUser fetchUserFromPrefs() {
        try {
            UUID userId = sharedPrefs.getUserId();
            String displayName = sharedPrefs.getDisplayName();
            boolean active = sharedPrefs.getActive();
            String accessToken = sharedPrefs.getAccessToken();
            Long expiresAt = sharedPrefs.getExpiresAt();
            String refreshToken = sharedPrefs.getRefreshToken();

            LoggedInUser user = new LoggedInUser();
            user.setUserId(userId);
            user.setDisplayName(displayName);
            user.setActive(active);
            user.setAccessToken(accessToken);
            user.setExpiresAt(expiresAt);
            user.setRefreshToken(refreshToken);

            return user;
        } catch (NullPointerException e) {
             Log.w(TAG, "fetching user from prefs failed, no one is logged in (probably)");
             return null;
        }
    }

    private void setLoggedInUser(LoggedInUser user) {
        authCache.setLoggedInUser(user);
        sharedPrefs.setAccessToken(user.getAccessToken());
        sharedPrefs.setExpiresAt(user.getExpiresAt());
        sharedPrefs.setRefreshToken(user.getRefreshToken());
        sharedPrefs.setDisplayName(user.getDisplayName());
        sharedPrefs.setActive(user.isActive());
        sharedPrefs.setUserId(user.getUserId());
    }

}
