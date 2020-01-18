package nl.authentication.management.app.data.login;


public class RegistrationFailedException extends Exception {
    RegistrationFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
