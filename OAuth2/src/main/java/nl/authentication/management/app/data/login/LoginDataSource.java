package nl.authentication.management.app.data.login;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import nl.authentication.management.app.api.AuthApi;
import nl.authentication.management.app.data.Result;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String TAG = "LoginDataSource";

    private final AuthApi authApi;

    @Inject
    public LoginDataSource(AuthApi authApi) {
        this.authApi = authApi;
    }

    Result<LoggedInUser> login(String username, String password) {

        try {
            Call<LoggedInUser> call = authApi.authenticateNative(username, password);
            Response<LoggedInUser> response = call.clone().execute();
            handleAuthResponse(response);

            return new Result.Success<>(response.body());
        } catch (Exception e) {
            Log.e(TAG, "Error while logging in: ", e);
            return new Result.Error(new LoginFailedException("Error logging in", e));
        }
    }

    Result<LoggedInUser> login(GoogleSignInAccount account) {
        try {
            Call<LoggedInUser> call = authApi.authenticateGoogle(account.getEmail(), account.getIdToken());
            Response<LoggedInUser> response = call.clone().execute();
            handleAuthResponse(response);

            return new Result.Success<>(response.body());
        } catch (Exception e) {
            Log.w(TAG, "error while logging in ", e);
            return new Result.Error(new LoginFailedException("Error logging in with google account", e));
        }
    }

    void logout(UUID uuid) {
        try {
            Call<Void> call = authApi.logout(uuid);
            call.clone().execute();
        } catch (IOException e) {
            Log.e(TAG, "I/O Error: ", e);
        }
    }

    private void handleAuthResponse(Response<LoggedInUser> response) throws Exception {
        switch (response.code()) {
            case 202:
                return;
            case 403:
                throw new LoginCredentialsInvalidException("Username or password was incorrect");
            default:
                throw new Exception(response.errorBody().string());
        }
    }

    public Result<Void> register(String username, String password) {
        try {
            Call<Void> call = authApi.register(username, password);
            Response<Void> response = call.clone().execute();
            if (response.isSuccessful()) {
                return new Result.Success<>(null);
            }
            return new Result.Error(new Exception(response.errorBody().string()));
        } catch (IOException e) {
            Log.e(TAG, "registration error: ", e);
            return new Result.Error(new RegistrationFailedException("Error registering.", e));
        }
    }
}
