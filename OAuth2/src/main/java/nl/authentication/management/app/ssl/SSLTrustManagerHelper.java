package nl.authentication.management.app.ssl;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class SSLTrustManagerHelper {

    private InputStream keyStore;
    private String keyStorePassword;
    private InputStream trustStore;
    private String trustStorePassword;

    public SSLTrustManagerHelper(InputStream keyStore,
                                 String keyStorePassword,
                                 InputStream trustStore,
                                 String trustStorePassword) throws ClientException {
        if (keyStore == null || keyStorePassword.trim().isEmpty() || trustStore == null || trustStorePassword.trim().isEmpty()) {
            throw new ClientException("TrustStore or KeyStore details are empty, which are required to be present when SSL is enabled");
        }

        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    public SSLContext clientSSLContext() throws ClientException {
        try {
            TrustManagerFactory trustManagerFactory = getTrustManagerFactory(trustStore, trustStorePassword);
            KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keyStore, keyStorePassword);

            return getSSLContext(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException | KeyManagementException e) {
            e.printStackTrace();
            throw new ClientException(e);
        } finally {
            try {
                this.trustStore.close();
                this.keyStore.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static SSLContext getSSLContext(KeyManager[] keyManagers, TrustManager[] trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }

    private static KeyManagerFactory getKeyManagerFactory(InputStream keystore, String keystorePassword) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, ClientException {
        KeyStore keyStore = loadKeyStore(keystore, keystorePassword);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
        return keyManagerFactory;
    }

    private static TrustManagerFactory getTrustManagerFactory(InputStream truststore, String truststorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, ClientException {
        KeyStore trustStore = loadKeyStore(truststore, truststorePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }

    private static KeyStore loadKeyStore(InputStream keystoreStream, String keystorePassword) throws ClientException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (keystoreStream == null) {
            throw new ClientException("keystore was null.");
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreStream, keystorePassword.toCharArray());
        return keystore;
    }

}