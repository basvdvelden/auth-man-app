package nl.authentication.management.app.ui.register;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.android.support.AndroidSupportInjection;
import nl.authentication.management.app.R;
import nl.authentication.management.app.di.DaggerViewModelFactory;
import nl.authentication.management.app.ui.AuthViewModel;


public class RegisterFragment extends Fragment {
    private AuthViewModel authViewModel;
    private NavController navController;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText password1EditText;
    private Button signUpButton;

    @Inject
    DaggerViewModelFactory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        authViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(AuthViewModel.class);
        usernameEditText = requireView().findViewById(R.id.username);
        passwordEditText = requireView().findViewById(R.id.password);
        password1EditText = requireView().findViewById(R.id.password1);
        signUpButton = requireView().findViewById(R.id.signUpButton);

        setListeners();
        setObservers();
    }

    private void setListeners() {
        signUpButton.setOnClickListener(v -> {
            authViewModel.register(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
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
                authViewModel.registerDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), password1EditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        password1EditText.addTextChangedListener(afterTextChangedListener);
    }

    private void setObservers() {
        authViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            signUpButton.setEnabled(registerFormState.isDataValid());
            if (registerFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registerFormState.getUsernameError()));
            }
            if (registerFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(registerFormState.getPasswordError()));
            }
            if (registerFormState.getPassword1Error() != null) {
                password1EditText.setError(getString(registerFormState.getPassword1Error()));
            }
        });
        authViewModel.getRegisterResult().observe(this, registerResult -> {
            if (registerResult == null) {
                return;
            }
            if (registerResult.getError() == null) {
                showToastMessage(R.string.registration_successful);
                navController.navigate(R.id.action_registerFragment_to_loginFragment);
            } else {
                passwordEditText.setText(null);
                password1EditText.setText(null);
                showToastMessage(registerResult.getError());
            }
            authViewModel.resetRegisterResult();
        });
    }

    private void showToastMessage(@StringRes Integer errorString) {
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show();
    }


}
