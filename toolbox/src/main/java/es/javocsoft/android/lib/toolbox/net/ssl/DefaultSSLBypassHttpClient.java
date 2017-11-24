package es.javocsoft.android.lib.toolbox.net.ssl;

import android.content.Context;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class is used to customize the behaviour of the Default HTTP client.
 * <br><br>
 * <b>IMPORTANT</b>: In this case, we ignore any SSL error in the connection. It is not recommendable
 * in production environemnts. To avoid the need of this class usage, try to use a valid server
 * certificate by a valid and known CA.
 *
 * @author JavocSoft, 2017
 * @since 2017
 * @version 1.0.0
 *
 */
public class DefaultSSLBypassHttpClient extends DefaultHttpClient {

    final Context context;

    /**
     * This Trust Manager accepts all certificates.
     */
    public static final TrustManager EasyTrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(
                X509Certificate[] chain,
                String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(
                X509Certificate[] chain,
                String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    /**
     * A custom DefaultHttpClient that accepts any certificate in a HTTPS connection.
     *
     * @param context
     */
    public DefaultSSLBypassHttpClient(Context context) {
        this.context = context;
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", newSslSocketFactory(), 443));
        return new SingleClientConnManager(getParams(), registry);
    }



    //AUXILIAR

    /**
     * Creates a custom SSL Socket Factory that trust ALL.
     */
    private MySSLSocketFactory newSslSocketFactory() {
        try {
            KeyStore trusted = KeyStore.getInstance("BKS");
            try {
                trusted.load(null, null);
            } finally {}

            MySSLSocketFactory sslfactory =  new MySSLSocketFactory(trusted);
            sslfactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sslfactory;

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Custom SSLSocketFactory that accepts any certificate ignoring
     * any SSL error.
     */
    public class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
