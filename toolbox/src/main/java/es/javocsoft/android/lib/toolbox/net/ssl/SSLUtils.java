package es.javocsoft.android.lib.toolbox.net.ssl;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import es.javocsoft.android.lib.toolbox.ToolBox;

/**
 * An utility class that can produce:
 *
 * <ul>
 *     <li>An {@link SSLContext} using an specified server certificate</li>
 *     <li>An {@link SSLSocketFactory} using the specified server certificate.</li>
 * </ul>
 *
 * To get the server certificate from the server just run:
 *
 * <pre>{@code
 * openssl s_client -debug -connect server:443
 * }</pre>
 *
 * @author JavocSoft 2017
 * @version 1.0.0
 */
public class SSLUtils {

    /**
     * Creates an SSLSocketFactory for the specified certificate file (X509). The
     * certificate must be in the assets folder of the specified application
     * context.
     *
     * @param context
     * @param fileName
     * @return
     * @throws Exception
     */
    public static SSLSocketFactory getSslSocketFactory4CertFile(Context context, String fileName) throws Exception {
        try {
            //We load the keystore just with the specified certificate
            KeyStore keyStore = SSLUtils.getKeyStore(context, fileName);

            return new SSLSocketFactory(keyStore);
        }
        catch (Exception e) {
            String msg = "Error creating SSLSocketFactory for certificate file " + fileName;
            Log.e(ToolBox.TAG, msg, e);

            throw new Exception(msg, e);
        }
    }

    /**
     * Creates an initialized SSLContext for the specified certificate file (X509). The
     * certificate must be in the assets folder of the specified application
     * context.
     *
     * @param context
     * @param fileName
     * @return
     */
    public static SSLContext getSslContext4CertFile(Context context, String fileName) {
        try {
            //We load the keystore just with the specified certificate
            KeyStore keyStore = SSLUtils.getKeyStore(context, fileName);

            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);

            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            return sslContext;
        }
        catch (Exception e) {
            String msg = "Error creating SslContext for certificate file " + fileName;
            Log.e(ToolBox.TAG, msg, e);

            throw new RuntimeException(msg);
        }
    }


    //AUXILIAR

    /**
     * Creates a keystore holding just the provided certificate.
     *
     * @param context
     * @param fileName
     * @return
     */
    private static KeyStore getKeyStore(Context context, String fileName) {
        KeyStore ksStore = null;

        try {
            AssetManager assetManager = context.getAssets();
            InputStream caInput = assetManager.open(fileName);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                String subjectDN = ((X509Certificate) ca).getSubjectDN().getName();
                if(ToolBox.LOG_ENABLE)
                    Log.i(ToolBox.TAG, "fileName (" + fileName + ") ca=" + subjectDN);
            } finally {
                caInput.close();
            }
            ksStore = KeyStore.getInstance(KeyStore.getDefaultType());
            ksStore.load(null, null);
            ksStore.setCertificateEntry("ca", ca);

        } catch (Exception e) {
            if(ToolBox.LOG_ENABLE)
                Log.e(ToolBox.TAG,"Could not get the keystore (" + e.getMessage() + ")", e);
        }

        return ksStore;
    }
}
