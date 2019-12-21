package nl.authentication.management.app.data.login;

public class PinVerificationFailedException extends Exception {
    public PinVerificationFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
