package nl.authentication.management.app.data.login;

public class FineAuthenticationFailedException extends Exception {
    public FineAuthenticationFailedException(String msg) {
        super(msg);
    }

    public FineAuthenticationFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
