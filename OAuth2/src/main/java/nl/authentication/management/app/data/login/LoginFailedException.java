package nl.authentication.management.app.data.login;

public class LoginFailedException extends Exception {
    LoginFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
