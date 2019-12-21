package nl.authentication.management.app.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class AuthorizationInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    // TODO: put in string resource file
    private static final String REFRESH_TOKEN_URL = "https://192.168.178.99:8443/oauth2/users/%s/token/refresh";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTH_HEADER_SCHEME = "Bearer ";

    private final AuthNotifier authNotifier;

    private static final class NoAuthUrls {
        private static final String BASE = "https://192.168.178.99:8443/oauth2";
        private static final String LOGIN_NATIVE = BASE.concat("/users/authenticate/native");
        private static final String LOGIN_GOOGLE = BASE.concat("/users/authenticate/google");
        // TODO: http method is not taken into account here!!
        private static final String REGISTER = BASE.concat("/users");
    }
    private final List<String> noAuthUrls = Arrays.asList(
            NoAuthUrls.LOGIN_GOOGLE,
            NoAuthUrls.LOGIN_NATIVE,
            NoAuthUrls.REGISTER
    );

    private AuthInfo authInfo;


    @Inject
    public AuthorizationInterceptor(AuthNotifier authNotifier) {
        this.authNotifier = authNotifier;
        this.authNotifier.getAuthInfo().subscribe(authInfo -> {
            this.authInfo = authInfo;
        });
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();
        String url = request.url().toString();

        Log.d(TAG, String.format("request url=%s", url));
        if (needsToken(url)) {
            // needs credentials
            String accessToken = authInfo.getTokens().getAccessToken();
            Response response;
            if (this.authInfo == null) {
                throw new RuntimeException("auth info is null, maybe login repository hasn't set it?");
            }
            if (isExpired(authInfo.getTokens().getExpiresAt())) {
                Request refreshTokenRequest = createRefreshTokenRequest(
                        authInfo.getTokens().getAccessToken(), request);

                Log.i(TAG, String.format("refreshing token, originalUrl=%s, user=%s",
                        request.url(), authInfo.getUserId()));

                response = chain.proceed(refreshTokenRequest);
                if (!response.isSuccessful()) {
                    Log.i(TAG, String.format("refreshing token failed, originalUrl=%s, user=%s",
                            request.url(), authInfo.getUserId()));

                    response.close();
                    response = chain.proceed(refreshTokenRequest);
                    if (!response.isSuccessful()) {
                        Log.w(TAG, String.format("refreshing token failed again, originalUrl=%s, " +
                                        "user=%s giving up...", request.url(), authInfo.getUserId()));
                        // notify login repository to logout
                        authNotifier.notifyToLogout();
                        return chain.proceed(requestBuilder.build());
                    }
                }
                Tokens result = extractTokens(response);

                // check if the original request was NOT logout, in which case we do save the tokens
                if (!request.url().toString().contains("logout")) {
                    handleTokensResponse(result);
                }
                // whether original request is logout or not, we still need access token for the request
                accessToken = result.getAccessToken();
                response.close();
            }

            requestBuilder.addHeader(AUTHORIZATION_HEADER, AUTH_HEADER_SCHEME + accessToken);

            response = chain.proceed(requestBuilder.build());
            return response;
        }

        return chain.proceed(requestBuilder.build());
    }

    private boolean isExpired(Long expiresAt) {
        return System.currentTimeMillis() + 3000 > expiresAt;
    }

    private void handleTokensResponse(Tokens tokens) {
        // notify login repository
        this.authNotifier.setTokens(tokens);
        // update our tokens
        this.authInfo.setTokens(tokens);
    }

    private Request createRefreshTokenRequest(String accessToken, Request oldRequest) {
        String refreshToken = authInfo.getTokens().getRefreshToken();
        RequestBody body = RequestBody.create(refreshToken, MediaType.get("text/plain"));
        return oldRequest
                .newBuilder()
                .post(body)
                .url(String.format(REFRESH_TOKEN_URL, authInfo.getUserId()))
                .addHeader(AUTHORIZATION_HEADER, AUTH_HEADER_SCHEME + accessToken)
                .build();
    }

    private Tokens extractTokens(Response response) throws IOException {
        return new Gson().fromJson(
                Objects.requireNonNull(response.body()).string(),
                Tokens.class
        );
    }

    private boolean needsToken(String url) {
        return !noAuthUrls.contains(url);
    }
}