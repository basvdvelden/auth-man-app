package nl.authentication.management.app.data.login;

public class AuthenticationFailedException extends Exception {
    public AuthenticationFailedException(String msg) {
        super(msg);
    }
}
