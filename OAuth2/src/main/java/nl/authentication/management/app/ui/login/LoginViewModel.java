package nl.authentication.management.app.ui.login;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import nl.authentication.management.app.R;
import nl.authentication.management.app.data.Result;
import nl.authentication.management.app.data.login.LoggedInUser;
import nl.authentication.management.app.data.login.LoginCredentialsInvalidException;
import nl.authentication.management.app.data.login.LoginFailedException;
import nl.authentication.management.app.data.login.LoginRepository;
import nl.authentication.management.app.ui.UIResult;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<UIResult<LoggedInUserView>> loginResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private EState state = EState.INITIAL;

    private enum EState {
        INITIAL,
        FILLING_FORM,
        LOGGED_IN
    }

    @Inject
    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        this.loginRepository.getIsLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isLoggedIn -> {
            Log.d(TAG, String.format("isLoggedIn=%s", isLoggedIn));
            this.isLoggedIn.setValue(isLoggedIn);
        });
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<UIResult<LoggedInUserView>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedIn;
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

    public void logout() {
        Log.d(TAG, "logging out...");
        LogoutTask task = new LogoutTask();
        task.execute();
    }

    public LoggedInUser getCurrentUser() {
        return loginRepository.getCurrentUser();
    }

    void setStateToFillingForm() {
        this.loginResult = new MutableLiveData<>();
        this.state = EState.FILLING_FORM;
    }

    void loginDataChanged(String username, String password) {
        this.state = EState.FILLING_FORM;
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private void handleLoginResult(Result<LoggedInUser> result) {
        state = EState.LOGGED_IN;
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

    // TODO: A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // TODO: A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
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

    private final class LogoutTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loginRepository.logout();
            return null;
        }
    }
}