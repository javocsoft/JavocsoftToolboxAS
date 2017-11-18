package es.javocsoft.android.lib.toolbox.firebase.core.actions;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;
import es.javocsoft.android.lib.toolbox.firebase.exception.FirebaseException;

/**
 * Unregisters the application from Firebase to stop receiving notifications.
 *
 * <br><br>
 * Firebase Notification Messaging (FCM) module.
 *
 * @author JavocSoft, 2017
 * @since  2017
 */
public class FirebaseUnRegistration extends Thread implements Runnable {

    private Context context;

    public FirebaseUnRegistration (Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            doUnRegistration();
            //We will receive an intent once the un-registration is done and
            //propagated in Google GCM servers. It can last up to 5 minutes.
        } catch (FirebaseException e) {
            ToolBox.prefs_savePreference(context, NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_UNREG_ERROR_CODE, Integer.class, e.errorCode);
        }
    }


    //AUXILIAR

    @SuppressWarnings({"MissingPermission"})
    private void doUnRegistration() throws FirebaseException {
        String regId = NotificationModule.getRegistrationId(context);
        if(regId!=null && regId.length()>0){
            try {
                //
                FirebaseInstanceId.getInstance().deleteInstanceId();

                if(NotificationModule.unregisterRunnable!=null &&
                        !NotificationModule.unregisterRunnable.isAlive()){
                    Thread t = new Thread(NotificationModule.unregisterRunnable);
                    t.start();

                    if(NotificationModule.LOG_ENABLE)
                        Log.i(NotificationModule.TAG, "ACK (unregister) executed.");
                }
            } catch (IOException e) {
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
                if(NotificationModule.LOG_ENABLE)
                    Log.i(NotificationModule.TAG, "Error unregistering from Firebase [" + e.getMessage() + "].");

                throw new FirebaseException(FirebaseException.ERROR_FIREBASE_UNREG, "Error unregistering from Firebase servers.");
            }
        }
    }
}
