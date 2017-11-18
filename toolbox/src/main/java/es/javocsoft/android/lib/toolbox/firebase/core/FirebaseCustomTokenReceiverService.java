package es.javocsoft.android.lib.toolbox.firebase.core;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnRegistrationCallback;
import es.javocsoft.android.lib.toolbox.firebase.exception.FirebaseException;

/**
 * This service attends to receive the Firebase registration id token. The service is called only
 * if Firebase determines that is neccesary to update the token.
 * <br><br>
 * Once get, it will store the registration token in the shared preferences
 * under the key {@link NotificationModule#FIREBASE_PREF_KEY_REGID} and also
 * the app version under {@link NotificationModule#FIREBASE_PREF_KEY_APP_VERSION}.
 * <br><br>
 * Finally, a callback {@link OnRegistrationCallback} is called if
 * was set in the notification module initialization.
 * <br><br>
 *
 * Set this code in your AndroidManifest.xml to declare it.
 * <br>
 *
 * <pre>{@code
 * <service android:name="es.javocsoft.android.lib.toolbox.firebase.core.FirebaseCustomTokenReceiverService">
 *      <intent-filter>
 *          <action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
 *      </intent-filter>
 * </service>
 * }</pre>
 *
 * Firebase STEP 1 - Firebase registration token received (ACK Registration)
 *
 * <br><br>
 * Firebase Notification Messaging (FCM) module.
 *
 * @author JavocSoft, 2017
 * @since  2017
 */

public class FirebaseCustomTokenReceiverService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated Firebase InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(NotificationModule.TAG, "Firebase token (refreshed): " + refreshedToken);

        if(refreshedToken!=null && refreshedToken.length()>0) {
            firebaseTokenOperations(refreshedToken);
        }else{
            ToolBox.prefs_savePreference(getApplicationContext(), NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_REG_ERROR_CODE, Integer.class, FirebaseException.ERROR_FIREBASE_GETTING_REGID);
        }
    }


    //AUXILIAR

    private void firebaseTokenOperations(String token) {
        NotificationModule.saveRegistrationId(getApplicationContext(), token);

        //We did not get any error.
        ToolBox.prefs_savePreference(getApplicationContext(), NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_REG_ERROR_CODE, Integer.class, FirebaseException.NO_ERROR_FIREBASE);

        //We launch the call, if exists one, for the registration callback
        if(NotificationModule.registerRunnable!=null &&
                !NotificationModule.registerRunnable.isAlive()){
            Thread t = new Thread(NotificationModule.registerRunnable);
            t.start();

            if(NotificationModule.LOG_ENABLE)
                Log.i(NotificationModule.TAG, "ACK (token refreshed) executed.");
        }
    }
}
