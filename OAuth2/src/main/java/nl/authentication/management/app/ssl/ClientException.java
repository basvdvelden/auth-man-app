package nl.authentication.management.app.ssl;

class ClientException extends Exception {
    ClientException(String msg) {
        super(msg);
    }

    ClientException(Exception e) {
        super(e);
    }
}
