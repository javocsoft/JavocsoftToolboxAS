package es.javocsoft.android.lib.toolbox.fingerprint;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Since Android Marshmallow, API Level 23, fingerprint authentication is available for any Android
 * device with a fingerprint sensor.<br><br>
 *
 * This helper class interacts with the fingerprint manager to perfom:
 * <ul>
 *     <li>Authentication</li>
 *     <li>Encryption of data with fingerprint authentication</li>
 *     <li>Decryption of data with fingerprint authentication</li>
 * </ul>
 *
 * <b>Note</b>: method used for encryption is AES. This is not the only option to encrypt, other
 * methods exist. In this helper the data is encrypted and decrypted in the following manner:
 * <br><br>
 *  Encryption:
 *  <ul>
 *      <li>User gives helper the desired non-encrypted data.</li>
 *      <li>User is required to provide fingerprint.</li>
 *      <li>Once authenticated, the helper obtains a key from the KeyStore and encrypts the data
 *      using a Cipher.</li>
 *      <li>Data and IV salt (IV is recreated for every encryption and is not reused) are saved to
 *      shared preferences to be used later in the decryption process.</li>
 *  </ul>
 *
 *  Decryption:
 *  <ul>
 *      <li>User requests to decrypt the data.</li>
 *      <li>User is required to provide fingerprint.</li>
 *      <li>The helper builds a Cipher using the IV and once user is authenticated, the KeyStore
 *      obtains a key from the KeyStore and deciphers the data.</li>
 *  </ul>
 *  <br>
 *  Usage instruction:<br><br>
 *
 *  1.- <b>Prepare the layout</b> by adding to your activity the <b>fingerprint informative dialog</b>
 *  part that shows when asking for fingerprint:
 *  <pre>{@code
 *  <RelativeLayout
 *        android:id="@+id/askFingerprint"
 *        android:layout_width="wrap_content"
 *        android:layout_height="wrap_content"
 *        android:layout_centerHorizontal="true"
 *        android:layout_centerVertical="true"
 *        android:layout_margin="10dp"
 *        android:visibility="gone"
 *        android:background="@drawable/roundedcorners_layout">
 *
 *        <ImageView
 *            android:id="@+id/imageViewFP"
 *            android:layout_width="wrap_content"
 *            android:layout_height="wrap_content"
 *            android:layout_marginTop="10dp"
 *            android:src="@drawable/ic_fingerprint_black_48dp"
 *            android:layout_centerHorizontal="true"/>
 *
 *        <TextView
 *            android:id="@+id/textViewFPInfo"
 *            android:layout_width="fill_parent"
 *            android:layout_height="wrap_content"
 *            android:layout_below="@+id/imageViewFP"
 *            android:layout_centerHorizontal="true"
 *            android:gravity="center_horizontal"
 *            android:layout_marginLeft="10dp"
 *            android:layout_marginRight="10dp"
 *            android:layout_marginBottom="10dp"
 *            android:text="@string/fingerprint_instructions"
 *            android:textAppearance="@android:style/TextAppearance.Medium" />
 *    </RelativeLayout>
 *  }</pre>
 *
 *  You can find "ic_fingerprint_black_48dp" in ToolBox or by looking for material resources at
 *  <a href="https://material.io/icons/#ic_fingerprint">Material resources</a>.<br><br>
 *
 * 2.- Now, declare the fingerprint informative layout to be ready in your whole activity class. Add
 * the following on your activity "onCreate" event:
 *
 * <pre>{@code
 * askFingerprint = (RelativeLayout) findViewById(R.id.askFingerprint);
 * }</pre>
 *
 * 3.- <b>Declare required fingerprint hardware and permissions</b> in your AndroidManifest.xml:
 *  <pre>{@code
 *  <uses-feature
 *    android:name="android.hardware.fingerprint"
 *    android:required="false" />
 *
 *  <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 *  }</pre>
 *
 * 4.- Declare your <b>cancellation signal</b>, accessible from the whole activity:
 * <pre>{@code
 *  CancellationSignal cancellationSignal;
 * }</pre>
 *
 * 5.- <b>Initialize the fingerprint helper</b> class:
 *
 * <pre>{@code
 *  //Initialize fingerprint reader helper
 *  FingerPrintAuthHelper fingerPrintAuthHelper = new FingerPrintAuthHelper(this);
 *  if (!fingerPrintAuthHelper.init()) {
 *      //If there was an error, show it
 *      ToolBox.toast_createCustomToast(getApplicationContext(),fingerPrintAuthHelper.getLastError(), ToolBox.TOAST_TYPE.ERROR, false);
 *  }
 * }</pre>
 *
 * 6.- Create your <b>fingerprint events callback listener</b>, "getAuthListener(boolean)":
 *
 *  <pre>
 *   &#64;NonNull
 *   private FingerPrintAuthHelper.Callback getAuthListener(final boolean isGetData) {
 *       return new FingerPrintAuthHelper.Callback() {
 *           &#64;Override
 *           public void onSuccess(String result) {
 *               ToolBox.device_vibrate(getApplicationContext(), 50l);
 *               askFingerprint.setVisibility(View.GONE);
 *
 *               if(result!=null &#38;&#38; result.equals(FingerPrintAuthHelper.FP_AUTHORIZED)) {
 *                   //We wanted only authorization
 *                   // Set here your stuff
 *
 *               }else{
 *                   if (isGetData) { //Encrypted data get
 *                       //Your stuff with decrypted data
 *                   } else { //Data was encrypted.
 *                       //Your stuff once encrypted
 *                   }
 *               }
 *           }
 *
 *           &#64;Override
 *           public void onFailure(String message) {
 *               ToolBox.toast_createCustomToast(getApplicationContext(), message, ToolBox.TOAST_TYPE.ERROR, false);
 *
 *               //Your stuff here
 *           }
 *
 *           &#64;Override
 *           public void onHelp(int helpCode, String helpString) {
 *               ToolBox.toast_createCustomToast(getApplicationContext(), helpCode + ": (" + helpString + ")", ToolBox.TOAST_TYPE.ERROR, false);
 *
 *               //Your stuff here
 *           }
 *       };
 *   }
 * </pre>
 *
 *  7.- Now you can start using the fingerprint helper class:<br><br>
 *
 *  To just authenticate with your fingerprint:
 *  <pre>{@code
 *    //Show fingerprint informative dialog
 *    askFingerprint.setVisibility(View.VISIBLE);
 *    //Create your cancellation signal to be able to abort fingerprint scanner normally.
 *    cancellationSignal = new CancellationSignal();
 *    //Ask for fingerprint authentication
 *    fingerPrintAuthHelper.auth(cancellationSignal, getAuthListener(false));
 *  }</pre>
 *
 *  To encrypt and save some data:
 *  <pre>{@code
 *    //Show fingerprint informative dialog
 *    askFingerprint.setVisibility(View.VISIBLE);
 *    //Create your cancellation signal to be able to abort fingerprint scanner normally.
 *    cancellationSignal = new CancellationSignal();
 *    //Ask for fingerprint encryption
 *    fingerPrintAuthHelper.saveData(String, cancellationSignal, getAuthListener(false));
 *  }</pre>
 *
 *  To recover some encrypted data:
 *  <pre>{@code
 *    //Show fingerprint informative dialog
 *    askFingerprint.setVisibility(View.VISIBLE);
 *    //Create your cancellation signal to be able to abort fingerprint scanner normally.
 *    cancellationSignal = new CancellationSignal();
 *    //Ask for fingerprint decryption
 *    fingerPrintAuthHelper.getData(cancellationSignal, getAuthListener(true));
 *  }</pre>
 *
 * 8.- You can always cancel fingerprint authentication, just use the declared cancellation signal
 * in your activity "onKeyDown" / "onBackPressed" events as follows:
 *
 * <pre>{
 *   &#64;RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
 *   &#64;Override
 *   public boolean onKeyDown(int keyCode, KeyEvent event)  {
 *       if (Integer.parseInt(android.os.Build.VERSION.SDK) &#60; 5
 *           &#38;&#38; keyCode == KeyEvent.KEYCODE_BACK
 *           &#38;&#38; event.getRepeatCount() == 0) {
 *           onBackPressed();
 *           return true;
 *       }
 *       return super.onKeyDown(keyCode, event);
 *   }
 *
 *   &#64;RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
 *   &#64;Override
 *   public void onBackPressed() {
 *       if(cancellationSignal!=null &#38;&#38; !cancellationSignal.isCanceled()){
 *           cancellationSignal.cancel();
 *           cancellationSignal = null;
 *
 *           //Hide fingerprint ask layout
 *           askFingerprint.setVisibility(View.GONE);
 *       }
 *   }
 * </pre>
 *
 *
 *  See also:<br><br>
 *      https://material.io/icons/#ic_fingerprint
 *      https://developer.android.com/about/versions/marshmallow/android-6.0.html
 *
 *  @author JavocSoft 2018
 *  @version 1.0
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintAuthHelper {

    private static final String FINGER_PRINT_HELPER = "FingerPrintAuthHelper";

    private static final String ENCRYPTED_DATA_SHARED_PREF_KEY = "ENCRYPTED_DATA_SHARED_PREF_KEY";
    private static final String LAST_USED_IV_SHARED_PREF_KEY = "LAST_USED_IV_SHARED_PREF_KEY";

    private static final String KS_APP_ALIAS = "J_FP_HELPER";

    public static final String FP_AUTHORIZED = "__FP_AUTH";

    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;

    private final Context context;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    private String lastError;


    /**
     * Callback to implements and use when using methods
     * {@link FingerPrintAuthHelper#saveData(String, CancellationSignal, Callback)},
     * {@link FingerPrintAuthHelper#getData(CancellationSignal, Callback)} and
     * {@link FingerPrintAuthHelper#auth(CancellationSignal, Callback)}.
     * <br><br>
     * Also see (see {@link FingerPrintAuthenticationListener}.
     */
    public interface Callback {

        /**
         * Is called when a fingerprint has been successfully matched to one of the fingerprints
         * stored on the user’s device.
         *
         * @param result    The decrypted data if the operation was decrypting or a message "Encrypted"
         *                  when data was sucessfully encrypted.
         */
        void onSuccess(String result);

        /**
         * Is called when the fingerprint doesn’t match with any of the fingerprints registered on
         * the device. You should inform the user about this event. Have in mind that there are
         * limited retries, once consumed, user will have to wait 30 seconds.
         *
         * @param message
         */
        void onFailure(String message);

        /**
         * Is called when a non-fatal error has occurred. This method provides additional
         * information about the error, so to provide the user with as much feedback as possible
         * you should inform to the user
         *
         * @param helpCode
         * @param helpString
         */
        void onHelp(int helpCode, String helpString);
    }


    /**
     * Fingerprint utility class. It can store encrypted data and decrypt that data by asking
     * for fingerprint validation.
     *
     * @param context
     */
    public FingerPrintAuthHelper(Context context) {
        this.context = context;
    }

    /**
     * Gets the last error.
     *
     * @return The error or null if there is none.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Checks if the user is able to use fingerprint sensor.
     * <br><br>
     * If all is correct, initializes the keystore to encrypt and decrypt data.
     *
     * @return True if fingerprint sensor can be used, false otherwise.
     */
    @SuppressLint("MissingPermission")
    public boolean init() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setError("This Android version does not support fingerprint authentication");
            return false;
        }

        keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);

        if (!fingerprintManager.isHardwareDetected()) {
            // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable
            // to use your app’s fingerprint functionality
            setError("Fingerprint sensor is not present");
            return false;
        }

        if (!keyguardManager.isKeyguardSecure()) {
            setError("User hasn't enabled Lock Screen");
            return false;
        }

        if (!hasPermission()) {
            setError("User hasn't granted permission to use Fingerprint");
            return false;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            setError("User hasn't registered any fingerprints");
            return false;
        }

        if (!initKeyStore()) {
            return false;
        }
        return false;
    }

    /**
     * Create sa Cipher object. It restores previously generated and used for encryption iv salt
     * when decryption mode or generates a new iv salt when encryption mode is on. See also
     * {@link FingerPrintAuthHelper#initKeyStore()}.
     *
     * @param mode  The ciphering mode. See {@link Cipher}
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    @Nullable
    private Cipher createCipher(int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, InvalidAlgorithmParameterException {
        //Create sthe cipher
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7);

        Key key = keyStore.getKey(KS_APP_ALIAS, null);
        if (key == null) {
            return null;
        }
        if(mode == Cipher.ENCRYPT_MODE) {
            cipher.init(mode, key);
            byte[] iv = cipher.getIV();
            //Saves in preferences the IV used to encrypt data for afterwards usage.
            saveIv(iv);
        } else {
            //recovers the IV used previously to encrypt data to be able to decrypt the encrypted data.
            byte[] lastIv = getLastIv();
            cipher.init(mode, key, new IvParameterSpec(lastIv));
        }

        return cipher;
    }

    /**
     * Creates the key generator.
     * @return
     */
    @NonNull
    private KeyGenParameterSpec createKeyGenParameterSpec() {
        return new KeyGenParameterSpec.Builder(KS_APP_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build();
    }

    /**
     * Initializes the keystore.
     *
     * @return  True if no error, otherwise False.
     */
    private boolean initKeyStore() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            if (getLastIv() == null) {
                KeyGenParameterSpec keyGeneratorSpec = createKeyGenParameterSpec();
                keyGenerator.init(keyGeneratorSpec);
                keyGenerator.generateKey();
            }
        } catch (Throwable t) {
            setError("Failed init of keyStore & keyGenerator: " + t.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Starts the fingerprint authentication process. Permission to use fingerprint sensor is also
     * checked. In case of any error, callback "onFailure" is called, see {@link Callback}
     *
     * @param cancellationSignal
     * @param authListener  See {@link FingerPrintAuthenticationListener}
     * @param mode
     */
    @SuppressLint("MissingPermission")
    private void authenticate(CancellationSignal cancellationSignal, FingerPrintAuthenticationListener authListener, int mode) {
        try {
            if (hasPermission()) {
                Cipher cipher = createCipher(mode);
                FingerprintManager.CryptoObject crypto = new FingerprintManager.CryptoObject(cipher);
                fingerprintManager.authenticate(crypto, cancellationSignal, 0, authListener, null);
            } else {
                authListener.getCallback().onFailure("User hasn't granted permission to use Fingerprint");
            }
        } catch (Throwable t) {
            authListener.getCallback().onFailure("An error occurred: " + t.getMessage());
        }
    }

    private String getSavedEncryptedData() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null) {
            return sharedPreferences.getString(ENCRYPTED_DATA_SHARED_PREF_KEY, null);
        }
        return null;
    }

    private void saveEncryptedData(String encryptedData) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putString(ENCRYPTED_DATA_SHARED_PREF_KEY, encryptedData);
        edit.commit();
    }

    /**
     * Gets the encryption used iv salt. This is done to be able to decrypt.
     *
     * @return
     */
    private byte[] getLastIv() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences != null) {
            String ivString = sharedPreferences.getString(LAST_USED_IV_SHARED_PREF_KEY, null);

            if (ivString != null) {
                return decodeBytes(ivString);
            }
        }
        return null;
    }

    /**
     * Saves the iv salt used for encryption.
     *
     * @param iv
     */
    private void saveIv(byte[] iv) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        String string = encodeBytes(iv);
        edit.putString(LAST_USED_IV_SHARED_PREF_KEY, string);
        edit.commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(FINGER_PRINT_HELPER, 0);
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Saves data after fingerprint check is passed.
     *
     * @param data  Data to save encrypted.
     * @param cancellationSignal
     * @param callback  See [@link {@link Callback}
     */
    public void saveData(@NonNull String data, CancellationSignal cancellationSignal, Callback callback) {
        authenticate(cancellationSignal, new FingerPrintEncryptDataListener(callback, data), Cipher.ENCRYPT_MODE);
    }

    /**
     * Triggers only AUTH through fingerprint.
     *
     * @param cancellationSignal
     * @param callback  See [@link {@link Callback}
     */
    public void auth(CancellationSignal cancellationSignal, Callback callback) {
        authenticate(cancellationSignal, new FingerPrintAuthListener(callback), Cipher.ENCRYPT_MODE);
    }

    /**
     * Get the encrypted stored data and decrypts once fingerprint validation is passed.
     *
     * @param cancellationSignal
     * @param callback  See [@link {@link Callback}
     */
    public void getData(CancellationSignal cancellationSignal, Callback callback) {
        authenticate(cancellationSignal, new FingerPrintDecryptDataListener(callback), Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypts data with the cipher obtained from key stored in the fingerprint protected keystore.
     *
     * @param cipher
     * @param data
     * @return
     */
    public boolean encryptData(Cipher cipher, String data) {
        try {
            // Encrypt the text
            if(data.isEmpty()) {
                setError("Password is empty");
                return false;
            }

            if (cipher == null) {
                setError("Could not create cipher");
                return false;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte[] bytes = data.getBytes(Charset.defaultCharset());
            cipherOutputStream.write(bytes);
            cipherOutputStream.flush();
            cipherOutputStream.close();
            saveEncryptedData(encodeBytes(outputStream.toByteArray()));
        } catch (Throwable t) {
            setError("Encryption failed " + t.getMessage());
            return false;
        }

        return true;
    }

    private byte[] decodeBytes(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if( len%2 != 0 )
            throw new IllegalArgumentException("hexBinary needs to be even-length: "+s);

        byte[] out = new byte[len/2];

        for( int i=0; i<len; i+=2 ) {
            int h = hexToBin(s.charAt(i  ));
            int l = hexToBin(s.charAt(i+1));
            if( h==-1 || l==-1 )
                throw new IllegalArgumentException("contains illegal character for hexBinary: "+s);

            out[i/2] = (byte)(h*16+l);
        }

        return out;
    }

    private static int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public String encodeBytes(byte[] data) {
        StringBuilder r = new StringBuilder(data.length*2);
        for ( byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    /**
     * Decrypts data with the cipher obtained from key stored in the fingerprint protected keystore.
     *
     * @param cipher
     * @return
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @NonNull
    private String decipher(Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        String retVal = null;
        String savedEncryptedData = getSavedEncryptedData();
        if (savedEncryptedData != null) {
            byte[] decodedPassword = decodeBytes(savedEncryptedData);
            CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(decodedPassword), cipher);

            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }
            cipherInputStream.close();

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < values.size(); i++) {
                bytes[i] = values.get(i).byteValue();
            }

            retVal = new String(bytes, Charset.defaultCharset());
        }
        return retVal;
    }

    /**
     * logs errors and set last error variable.
     *
     * @param error
     */
    private void setError(String error) {
        lastError = error;
        Log.w(FINGER_PRINT_HELPER, lastError);
    }


    //Listeners ------------------------------------------------------------------------------------

    //Fingerprint listener

    /**
     * This is the callback called by Android Fingerprint Operation result
     */
    protected class FingerPrintAuthenticationListener extends FingerprintManager.AuthenticationCallback {

        protected final Callback callback;

        /**
         * Operates with the result and calls to the specified callback.
         *
         * @param callback
         */
        public FingerPrintAuthenticationListener(@NonNull Callback callback) {
            this.callback = callback;
        }

        public void onAuthenticationError(int errorCode, CharSequence errString) {
            //Toast.makeText(context, "Fingerprint authentication error", Toast.LENGTH_LONG).show();
            callback.onFailure("Authentication error [" + errorCode + "] " + errString);
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         *
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            callback.onHelp(helpCode, helpString.toString());
        }

        /**
         * Called when a fingerprint is recognized.
         *
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            //Toast.makeText(context, "Fingerprint recognized", Toast.LENGTH_LONG).show();
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() {
            //Toast.makeText(context, "Fingerprint not recognized", Toast.LENGTH_LONG).show();
            callback.onFailure("Authentication failed");
        }

        public @NonNull
        Callback getCallback() {
            return callback;
        }
    }

    //Auth/Encryption/Decryption listeners

    /**
     *  Listener for fingerprint based AUTH basic operation.
     */
    private class FingerPrintAuthListener extends FingerPrintAuthenticationListener {

        /**
         * Encryption listener.
         *
         * @param callback  See {@link Callback}
         */
        public FingerPrintAuthListener(Callback callback) {
            super(callback);
        }

        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);

            callback.onSuccess(FP_AUTHORIZED);
        }
    }

    /**
     *  Listener for fingerprint based encryptions operations.
     */
    private class FingerPrintEncryptDataListener extends FingerPrintAuthenticationListener {

        private final String data;

        /**
         * Encryption listener.
         *
         * @param callback  See {@link Callback}
         * @param data  The data to encrypt.
         */
        public FingerPrintEncryptDataListener(Callback callback, String data) {
            super(callback);
            this.data = data;
        }

        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);

            //We add some behaviour to on success
            Cipher cipher = result.getCryptoObject().getCipher();
            try {
                if (encryptData(cipher, data)) {
                    callback.onSuccess("Encrypted");
                } else {
                    callback.onFailure("Encryption failed");
                }

            } catch (Exception e) {
                callback.onFailure("Encryption failed " + e.getMessage());
            }
        }
    }

    /**
     * Listener for fingerprint based decryptions operations.
     */
    protected class FingerPrintDecryptDataListener extends FingerPrintAuthenticationListener {

        /**
         * Decryption listener.
         *
         * @param callback  See {@link Callback}
         */
        public FingerPrintDecryptDataListener(@NonNull Callback callback) {
            super(callback);
        }

        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);

            //We add some behaviour to on success
            Cipher cipher = result.getCryptoObject().getCipher();
            try {
                String decryptedData = decipher(cipher);
                if (decryptedData != null) {
                    callback.onSuccess(decryptedData);
                } else {
                    callback.onFailure("Failed decryption");
                }

            } catch (Exception e) {
                callback.onFailure("Decryption failed " + e.getMessage());
            }
        }
    }

}
