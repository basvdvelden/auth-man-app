package nl.authentication.management.app.ui.register;

import androidx.annotation.Nullable;

/**
 * Data validation state of registration form.
 */
public class RegisterFormState {
    @Nullable
    private Integer usernameError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer password1Error;
    private boolean isDataValid;

    public RegisterFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer password1Error) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.password1Error = password1Error;
        this.isDataValid = false;
    }

    public RegisterFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.password1Error = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getPassword1Error() {
        return password1Error;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}
