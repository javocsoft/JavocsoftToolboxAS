package es.javocsoft.android.lib.toolbox.firebase.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.Constructor;
import java.util.Map;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnNewNotificationCallback;

/**
 * This broadcast receiver receives the Firebase notifications.
 * <br><br>
 * Set this code in your AndroidManifest.xml to declare it.
 * <br>
 *
 * <pre>{@code
 * <service android:name="es.javocsoft.android.lib.toolbox.firebase.core.NotificationOpenedReceiver">
 * 	<intent-filter>
 * 		<action android:name="com.google.firebase.MESSAGING_EVENT"/>
 *	</intent-filter>
 * </service>
 * }</pre>
 *
 * Firebase STEP 2 - Notification received from Firebase (NotificationProcessService is called)
 * <br><br>
 * <b>IMPORTANT NOTES</b>
 * <br>
 * As seen in the Handling Messages for Android FCM docs, if the payload you sent has both
 * notification and data, it will be handled separately.
 * <ul>
 *     <li>The notification part will be handled by the Notification Tray (calling to handleIntent)</li>
 *     <li>The data part will be in the extras of the intent (calling to onMessageReceived)</li>
 * </ul>
 *
 * Also:
 * <ul>
 *     <li><b>There is no way to get the notification payload (data) when the app is in
 *     background</b> (always handled by the Notification Tray)</li>
 *     <li><b>If the notification does not have a "data" part and the application is not in
 *     foreground, the method "onMessageReceived" will not be fired</b>. The Google Firebase developer
 *     console does not sent the body in the "data" payload.</li>
 * </ul>
 *
 * <b>ADVISE</b>
 * So, if you need to send some parameters in the notification, use always "data" section in your
 * server requests to FCM. This module will look for any parameter starting with "fcmp" preffix in
 * the notification part, if you do not use this preffix, the module will try to guess them by
 * excluding notification parameters that start with "google.", "gcm.", "from" and "collapse_key".
 *
 * <br><br>
 * Firebase Notification Messaging (FCM) module.
 *
 * @author JavocSoft, 2017
 * @since  2017
 */
public class FirebaseCustomNotificationReceiver extends FirebaseMessagingService {

    //@Override
    public void handleIntent(Intent intent) {
        //NOTE
        //We comment this to avoid default Notification Try notification because we want our own
        //custom behaviour.
        //super.handleIntent(intent);

        if(NotificationModule.LOG_ENABLE)
            Log.i(NotificationModule.TAG, "FirebaseCustomNotificationReceiver (handleIntent) message received");

        // When overriding this method, the other one "onMessageReceived" is never called.
        handleMessage(intent.getExtras());
    }

    @Override
    /**
     * NOTE: Never called when "handleIntent(Intent intent)" is overrided. When the application is
     * not in foreground, this method is also never called.
     */
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(NotificationModule.LOG_ENABLE)
            Log.i(NotificationModule.TAG, "FirebaseCustomNotificationReceiver message received from " + remoteMessage.getFrom());

        handleMessage(remoteMessage);
    }

    /**
     * This will process the notification receival but will not have any of the data stored in the
     * "data" section.<br>
     *
     * IMPORTANT: This is only called when the notification does not have a "data" section.
     *
     * @param extras
     */
    private void handleMessage(Bundle extras) {

        // Check if message contains a data payload.
        if (extras.size() > 0) {
            if(NotificationModule.LOG_ENABLE)
                Log.i(NotificationModule.TAG, "Message data payload: " + bundleToString(extras));

            //Package must read from shared preferences because the application is not running
            String appPackage = (String)ToolBox.prefs_readPreference(getApplicationContext(), NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_APPPACKAGE, String.class);
            if(NotificationModule.LOG_ENABLE)
                Log.i(NotificationModule.TAG, "Application package: " + appPackage);

            //data payload:
            // google.c.a.c_l testFB (java.lang.String),
            // google.c.a.udt 0 (java.lang.String),
            // . google.sent_time 1510843526091 (java.lang.Long),
            // gcm.notification.e 1 (java.lang.String),
            // google.c.a.c_id 4394850702641698012 (java.lang.String),
            // google.c.a.ts 1510843526 (java.lang.String),
            // . gcm.notification.sound default (java.lang.String),
            // gcm.n.e 1 (java.lang.String),
            // c1 1 (java.lang.String),
            // . from 889817437597 (java.lang.String),
            // gcm.notification.sound2 default (java.lang.String),
            // . google.message_id 0:1510843530835317%91d7f25291d7f252 (java.lang.String),
            // . gcm.notification.body noyo (java.lang.String),
            // google.c.a.e 1 (java.lang.String),
            // . collapse_key com.colectivosvip.testingfirebase (java.lang.String)
            processBundleForNotification(extras);

            //1.- Generate the notification in the task bar.
            generateNotification(getApplicationContext(), extras);

            //2.- Run the runnable set for a new notification received event.
            launchOnNewNotificationEventRunnable(getApplicationContext(), extras);
        }
    }

    /**
     * This will handle the received notification (including the "data" part).
     * <br><br>
     * IMPORTANT: Whenever the notification has the "data" part, this method will handle the
     * notificaton.
     *
     * @param remoteMessage
     */
    private void handleMessage(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if(NotificationModule.LOG_ENABLE)
                Log.i(NotificationModule.TAG, "Message data payload: " + remoteMessage.getData());

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                if(NotificationModule.LOG_ENABLE)
                    Log.i(NotificationModule.TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            Bundle extras = new Bundle();
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                extras.putString(entry.getKey(), entry.getValue());
            }
            if(remoteMessage.getData().get(NotificationModule.ANDROID_NOTIFICATION_MESSAGE_KEY)==null){
                extras.putString(NotificationModule.ANDROID_NOTIFICATION_MESSAGE_KEY, remoteMessage.getNotification().getBody());
            }
            /* We set other default properties of the received FCM notification */
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_FROM_KEY, remoteMessage.getFrom());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_COLLAPSEKEY_KEY, remoteMessage.getCollapseKey());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_MESSAGEID_KEY, remoteMessage.getMessageId());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_TO_KEY, remoteMessage.getTo());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_SENTTIME_KEY, String.valueOf(remoteMessage.getSentTime()));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_TTLENABLED_KEY, String.valueOf(remoteMessage.getTtl()));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_CLICKACTION_KEY, remoteMessage.getNotification().getClickAction());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_COLOR_KEY, remoteMessage.getNotification().getColor());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_ICON_KEY, remoteMessage.getNotification().getIcon());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_SOUND_KEY, remoteMessage.getNotification().getSound());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_TAG_KEY, remoteMessage.getNotification().getTag());
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_TIT_KEY, remoteMessage.getNotification().getTitle());

            //1.- Generate the notification in the task bar.
            generateNotification(getApplicationContext(), extras);

            //2.- Run the runnable set for a new notification received event.
            launchOnNewNotificationEventRunnable(getApplicationContext(), extras);
        }
    }

    /**
     * Extracts the intent bundle notification information to construct
     * a valid Bundle containing the required parameters and also the data
     * payload.
     * <br><br>
     * <b>Notes</b><br>
     * Data payload parameters should start with the prefix "fcmp" when sending the notification.
     *
     * @param extras
     * @return
     */
    private void processBundleForNotification(Bundle extras) {

        if(extras!=null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                //We save any possible parameter set in the data payload of the notification.
                if(key.startsWith("fcmp")){
                    //If data payload parameters start with this prefix, we add them.
                    extras.putString(key, value.toString());
                }else{
                    //TODO Improve this.
                    //We try to remove any possible non data payload information
                    if(!key.startsWith("google.") && !key.startsWith("gcm.")
                            && !key.equalsIgnoreCase("from") && !key.equalsIgnoreCase("collapse_key")){
                        extras.putString(key, value.toString());
                    }
                }
            }

            if(extras.getString(NotificationModule.ANDROID_NOTIFICATION_MESSAGE_KEY)==null){
                extras.putString(NotificationModule.ANDROID_NOTIFICATION_MESSAGE_KEY, extras.getString("gcm.notification.body"));
            }
            /* We set other default properties of the received FCM notification */
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_FROM_KEY, extras.getString("from"));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_COLLAPSEKEY_KEY, extras.getString("collapse_key"));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_MESSAGEID_KEY, extras.getString("google.message_id"));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_SENTTIME_KEY, String.valueOf(extras.getLong("google.sent_time")));
            extras.putString(NotificationModule.ANDROID_NOTIFICATION_SOUND_KEY, extras.getString("gcm.notification.sound"));
        }
    }

    /**
     * Converts a Bundle into an String containing all data.
     *
     * @param bundle
     * @return
     */
    private String bundleToString(Bundle bundle) {
        String res = null;
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                if(res==null)
                    res = "";
                else
                    res+=",";

                Object value = bundle.get(key);
                res+=(String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
        Log.d(NotificationModule.TAG, res);

        return res;
    }

    /**
     * Gets the string content from the notification bundle.
     *
     * @param extras
     * @param key
     * @return
     */
    private String getNotificationKeyContent(Bundle extras, String key) {
        String res = null;

        if(extras!=null && extras.get(key)!=null){
            res = (String)extras.get(key);
        }

        return res;
    }

    /**
     * Gets the string content from the notification intent extra information.
     *
     * @param intent
     * @param key
     * @return
     */
    @SuppressWarnings("unused")
    private String getNotificationKeyContent(Intent intent, String key) {
        String res = null;

        if(intent!=null && intent.hasExtra(key)){
            res = intent.getStringExtra(key);
        }

        return res;
    }

    /**
     * Try to launch the configured runnable (if set) from the
     * Firebase OnNewNotification received event.
     *
     * @param extras
     */
    private void launchOnNewNotificationEventRunnable(Context context, Bundle extras) {
        //2.- Run the runnable set for a new notification received event.
        if(NotificationModule.doWhenNotificationRunnable==null) {
            //...if null, we try to get the runnable from the saved configuration
            //in the SharedPreferences. This could happen if the app was closed because
            //the service does not know nothing when is called in this case.
            String notificationOnNotReceivedThreadToCall = (String) ToolBox.prefs_readPreference(context.getApplicationContext().getApplicationContext(),
                    NotificationModule.FIREBASE_PREF_NAME,
                    NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_ONNOTRECEIVEDTHREAD_TO_CALL, String.class);
            Log.i(NotificationModule.TAG, "Runnable to run when a new notification is received: " + notificationOnNotReceivedThreadToCall);

            if(notificationOnNotReceivedThreadToCall!=null) {
                //Get a new instance from the class with reflection.
                try{
                    Class<?> c = Class.forName(notificationOnNotReceivedThreadToCall);
                    Constructor<?> cons = c.getConstructor();
                    Object onNewNotificationRunnableObject = cons.newInstance();
                    NotificationModule.doWhenNotificationRunnable = (OnNewNotificationCallback)onNewNotificationRunnableObject;
                    NotificationModule.doWhenNotificationRunnable.context = context.getApplicationContext();

                }catch(Exception e) {
                    if(NotificationModule.LOG_ENABLE)
                        Log.e(NotificationModule.TAG,"Runnable for a new notification received could not be run. " +
                                "Class could not be found/get (" + e.getMessage() + ").", e);
                }
            }else{
                if(NotificationModule.LOG_ENABLE)
                    Log.i(NotificationModule.TAG,"No Runnable specified for a new notification received event.");
            }
        }

        //Do something when new notification arrives.
        if(NotificationModule.doWhenNotificationRunnable!=null &&
                !NotificationModule.doWhenNotificationRunnable.isAlive()){
            //Set the intent extras
            NotificationModule.doWhenNotificationRunnable.setNotificationBundle(extras);
            //Launch the thread
            Thread t = new Thread(NotificationModule.doWhenNotificationRunnable);
            t.start();

            if(NotificationModule.LOG_ENABLE)
                Log.i(NotificationModule.TAG, "ACK (received) executed.");
        }
    }

    /**
     * Creates the Android system notification to alert the user.
     *
     * @param context
     * @param extras
     */
    private void generateNotification(Context context, Bundle extras) {

        String notStyle = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_STYLE_KEY);
        String notTitle = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_TITLE_KEY);
        String notMessage = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_MESSAGE_KEY);
        String notTicker = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_TICKER_KEY);
        String notContentInfo = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_CONTENT_INFO_KEY);
        String notBigStyleTitle = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_TITLE_KEY);
        String notBigStyleContent = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_CONTENT_KEY);
        String notBigStyleSummary = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_SUMMARY_KEY);
        String notBigStyleImage = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_IMAGE_KEY);
        String notBigStyleLargeIcon = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_LARGE_ICON_KEY);
        String notBigStyleInboxContent = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_INBOX_CONTENT_KEY);
        String notBigStyleInboxLineSeparator = getNotificationKeyContent(extras, NotificationModule.ANDROID_NOTIFICATION_BIG_STYLE_INBOX_LINE_SEPARATOR_KEY);

        //This service must be isolated from the possible status of the app to be able to
        //do its job even if the app is closed. This is the reason we get the required stuff
        //from the previously saved information in SharedPreferences.
        String notificationActToCall = (String)ToolBox.prefs_readPreference(context, NotificationModule.FIREBASE_PREF_NAME,
                NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_ACTIVITY_TO_CALL, String.class);
        NotificationModule.NOTIFICATION_TITLE = (String)ToolBox.prefs_readPreference(context, NotificationModule.FIREBASE_PREF_NAME,
                NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_TITLE, String.class);
        NotificationModule.multipleNot = (Boolean)ToolBox.prefs_readPreference(context, NotificationModule.FIREBASE_PREF_NAME,
                NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_MULTIPLENOT, Boolean.class);
        NotificationModule.groupMultipleNotKey = (String)ToolBox.prefs_readPreference(context, NotificationModule.FIREBASE_PREF_NAME,
                NotificationModule.FIREBASE_PREF_KEY_APP_NOTIFICATION_GROUPMULTIPLENOTKEY, String.class);

        try{
            //Get the Activity class to call when notification is opened.
            Class<?> clazz = Class.forName(notificationActToCall);
            NotificationModule.NOTIFICATION_ACTIVITY_TO_CALL = clazz;

            //Get the notification style.
            ToolBox.NOTIFICATION_STYLE nStyle = ToolBox.NOTIFICATION_STYLE.NORMAL_STYLE;
            try{
                if(notStyle!=null && notStyle.length()>0){
                    nStyle = ToolBox.NOTIFICATION_STYLE.valueOf(notStyle);
                }
            }catch(Exception e) {/* Invalid value, we use the NORMAL_STYLE. */}


            ToolBox.notification_create(context,
                    true, null, false,
                    NotificationModule.multipleNot, NotificationModule.groupMultipleNotKey,
                    NotificationModule.NOTIFICATION_ACTION_KEY,
                    (notTitle!=null?notTitle:NotificationModule.NOTIFICATION_TITLE), notMessage,
                    notTicker, notContentInfo,
                    notBigStyleTitle, notBigStyleContent,
                    notBigStyleSummary, notBigStyleImage,
                    notBigStyleInboxContent, notBigStyleInboxLineSeparator,
                    NotificationModule.notBackgroundColor,
                    NotificationModule.NOTIFICATION_ACTIVITY_TO_CALL,
                    extras, false,
                    ToolBox.NOTIFICATION_PRIORITY.DEFAULT,
                    nStyle,
                    ToolBox.NOTIFICATION_LOCK_SCREEN_PRIVACY.PRIVATE,
                    (notBigStyleLargeIcon!=null?notBigStyleLargeIcon:null),
                    null,
                    null,
                    ToolBox.NOTIFICATION_PROGRESSBAR_STYLE.NONE,
                    null, null,
                    null, NotificationModule.vibrate);


            if(NotificationModule.LOG_ENABLE)
                Log.d(NotificationModule.TAG, "Notification created for the recieve PUSH message.");


        }catch(Exception e) {
            if(NotificationModule.LOG_ENABLE)
                Log.e(NotificationModule.TAG,"Notification could not be created for the received PUSH message. " +
                        "Notification activity to open class could not be found (" + e.getMessage() + ").", e);
        }
    }
}
