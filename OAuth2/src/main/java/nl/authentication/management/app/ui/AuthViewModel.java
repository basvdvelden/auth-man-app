package nl.authentication.management.app.ui;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.gosimple.nbvcxz.Nbvcxz;
import nl.authentication.management.app.R;
import nl.authentication.management.app.data.Result;
import nl.authentication.management.app.data.login.LoggedInUser;
import nl.authentication.management.app.data.login.LoginCredentialsInvalidException;
import nl.authentication.management.app.data.login.LoginFailedException;
import nl.authentication.management.app.data.login.LoginRepository;
import nl.authentication.management.app.ui.login.LoggedInUserView;
import nl.authentication.management.app.ui.login.LoginFormState;
import nl.authentication.management.app.ui.register.RegisterFormState;

@Singleton
public class AuthViewModel extends ViewModel {
    private static final String TAG = "AuthViewModel";

    // login
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<UIResult<LoggedInUserView>> loginResult = new MutableLiveData<>();

    // register
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<UIResult<Void>> registerResult = new MutableLiveData<>();
    private Observable current;

    private LoginRepository loginRepository;

    @Inject
    public AuthViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<UIResult<LoggedInUserView>> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        Log.d(TAG, "logging in...");
        NativeLoginTask task = new NativeLoginTask();
        task.execute(username, password);
    }

    public void login(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "logging in...");
        GoogleLoginTask task = new GoogleLoginTask();
        task.execute(googleSignInAccount);
    }

    public void loginDataChanged(String username, String password) {
        Integer usernameError = getUsernameError(username);

        if (usernameError != null) {
            loginFormState.setValue(new LoginFormState(usernameError, null));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private void handleLoginResult(Result<LoggedInUser> result) {
        if (result instanceof Result.Success) {
            LoggedInUser loggedInUser = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new UIResult<>(new LoggedInUserView(loggedInUser.getDisplayName())));
        } else {
            LoginFailedException error = (LoginFailedException)
                    ((Result.Error) result).getError();

            Throwable cause = error.getCause();
            if (cause instanceof LoginCredentialsInvalidException) {
                Log.w(TAG, "login credentials invalid");
                loginResult.setValue(new UIResult<>(R.string.login_credentials_incorrect));
            } else {
                Log.e(TAG, "login failed ERROR: ", cause);
                loginResult.setValue(null);
            }
        }
    }

    public void register(String username, String password) {
        Log.d(TAG, "registering...");
        RegisterTask task = new RegisterTask();
        task.execute(username, password);
    }

    public MutableLiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public MutableLiveData<UIResult<Void>> getRegisterResult() {
        return registerResult;
    }

    public void registerDataChanged(String username, String password, String password1) {
        Observable<RegisterFormState> obs = Observable.fromCallable(() -> {
            Integer usernameError = getUsernameError(username);
            Integer passwordError = getPasswordError(password);
            Integer password1Error = getPassword1Error(password, password1);
            RegisterFormState state = new RegisterFormState(true);
            if (usernameError != null) {
                state = new RegisterFormState(usernameError, null, null);
            } else if (passwordError != null) {
                state = new RegisterFormState(null, passwordError, null);
            } else if (password1Error != null) {
                state = new RegisterFormState(null, null, password1Error);
            }
            return state;
        });
        current = obs;
        obs.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                    if (current == obs) {
                        registerFormState.setValue(state);
                    }
                });
    }

    private void handleRegisterResult(Result<Void> result) {
        if (result instanceof Result.Success) {
            Log.d(TAG, "registration successful");
            registerResult.setValue(new UIResult<>(null));
        } else {
            Log.d(TAG, "registration failed");
            registerResult.setValue(new UIResult<>(R.string.registration_failed));
        }
    }

    public LoggedInUser getCurrentUser() {
        return loginRepository.getCurrentUser();
    }

    public void logout() {
        Log.d(TAG, "logging out...");
        LogoutTask task = new LogoutTask();
        task.execute();
    }

    public void resetLoginResult() {
        this.loginResult = new MutableLiveData<>();
    }

    public void resetRegisterResult() {
        this.registerResult = new MutableLiveData<>();
    }

    private Integer getUsernameError(String username) {
        if (username == null || username.trim().isEmpty()) {
            return R.string.username_empty;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches() ? null : R.string.invalid_email;
        }
        return null;
    }

    private Integer getPasswordError(String password) {
        if (password.trim().isEmpty()) {
            return R.string.password_empty;
        }
        Nbvcxz nbvcxz = new Nbvcxz();
        me.gosimple.nbvcxz.scoring.Result result = nbvcxz.estimate(password);
        if (!result.isMinimumEntropyMet()) {
            Log.d(TAG, result.getFeedback().getWarning());
            Log.d(TAG, result.getFeedback().getSuggestion().toString());
            return R.string.invalid_password;
        }
        return null;
    }

    private Integer getPassword1Error(String password, String password1) {
        Boolean valid = password.equals(password1);
        return valid ? null : R.string.passwords_dont_match;
    }

    private final class NativeLoginTask extends AsyncTask<String, Void, Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(String... credentials) {
            return loginRepository.login(credentials[0], credentials[1]);
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result) {
            handleLoginResult(result);
        }
    }

    private final class GoogleLoginTask extends AsyncTask<GoogleSignInAccount, Void, Result<LoggedInUser>> {

        @Override
        protected Result<LoggedInUser> doInBackground(GoogleSignInAccount... googleSignInAccounts) {
            return loginRepository.login(googleSignInAccounts[0]);
        }

        @Override
        protected void onPostExecute(Result<LoggedInUser> result) {
            handleLoginResult(result);
        }
    }

    private final class RegisterTask extends AsyncTask<String, Void, Result<Void>> {

        @Override
        protected Result<Void> doInBackground(String... registerFormValues) {
            return loginRepository.register(registerFormValues[0], registerFormValues[1]);
        }

        @Override
        protected void onPostExecute(Result<Void> result) {
            handleRegisterResult(result);
        }
    }

    private final class LogoutTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loginRepository.logout();
            return null;
        }
    }
}