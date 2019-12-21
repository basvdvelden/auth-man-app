package nl.authentication.management.app.data.login;

public class LoginCredentialsInvalidException extends RuntimeException {
    LoginCredentialsInvalidException(String msg) {
        super(msg);
    }
}
