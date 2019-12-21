package nl.authentication.management.app.data.login;

class NotYetRegisteredException extends RuntimeException {
    public NotYetRegisteredException(String msg) {
        super(msg);
    }
}
