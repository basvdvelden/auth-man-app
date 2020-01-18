package nl.authentication.management.app.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import javax.inject.Inject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.android.support.AndroidSupportInjection;
import nl.authentication.management.app.R;
import nl.authentication.management.app.di.DaggerViewModelFactory;
import nl.authentication.management.app.ui.AuthViewModel;


public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final int GOOGLE_LOGIN_REQUEST_CODE = 101;

    private AuthViewModel authViewModel;
    private ProgressBar loadingProgressBar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private NavController navController;
    private Button loginButton;
    private SignInButton googleLoginButton;
    private Button signUpButton;
    private GoogleSignInClient googleSignInClient;

    @Inject
    DaggerViewModelFactory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        authViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(AuthViewModel.class);

        String clientId = getString(R.string.google_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        setLoadingProgressBar(view.findViewById(R.id.loading));

        setUsernameEditText(view.findViewById(R.id.username));
        setPasswordEditText(view.findViewById(R.id.password));

        setLoginButton(view.findViewById(R.id.login));
        setGoogleLoginButton(view.findViewById(R.id.sign_in_button));
        signUpButton = view.findViewById(R.id.sign_up_button);

        setGoogleSignInClient(GoogleSignIn.getClient(requireActivity(), gso));

        setListeners();
        setObservers();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("Activity result request code: %d", resultCode));
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_LOGIN_REQUEST_CODE) {
                try {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    authViewModel.login(account);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToastMessage(@StringRes Integer errorString) {
        Toast.makeText(getContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            authViewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });
        googleLoginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_LOGIN_REQUEST_CODE);
        });
        signUpButton.setOnClickListener(v -> {
            navController.navigate(R.id.action_loginFragment_to_registerFragment);
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                authViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                authViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });
    }

    private void setObservers() {
        authViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        authViewModel.getLoginResult().observe(this, loginResult -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult == null) {
                return;
            }
            if (loginResult.getError() != null) {
                passwordEditText.setText(null);
                showToastMessage(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                showToastMessage(R.string.login_success);
            }
            authViewModel.resetLoginResult();
        });
    }

    private void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    private void setLoadingProgressBar(ProgressBar loadingProgressBar) {
        this.loadingProgressBar = loadingProgressBar;
    }

    private void setUsernameEditText(EditText usernameEditText) {
        this.usernameEditText = usernameEditText;
    }

    private void setPasswordEditText(EditText passwordEditText) {
        this.passwordEditText = passwordEditText;
    }

    private void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    private void setGoogleLoginButton(SignInButton googleLoginButton) {
        this.googleLoginButton = googleLoginButton;
    }
}

