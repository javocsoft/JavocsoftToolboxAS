/*
 * Copyright (C) 2010-2014 - JavocSoft - Javier Gonzalez Serrano
 * http://javocsoft.es/proyectos/code-libs/android/javocsoft-toolbox-android-library
 * 
 * This file is part of JavocSoft Android Toolbox library.
 *
 * JavocSoft Android Toolbox library is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 *
 * JavocSoft Android Toolbox library is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavocSoft Android Toolbox library.  If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 */
package es.javocsoft.android.lib.toolbox.firebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.firebase.core.actions.FirebaseUnRegistration;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnNewNotificationCallback;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnOpenAckCallback;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnRegistrationCallback;
import es.javocsoft.android.lib.toolbox.firebase.core.callback.OnUnregistrationCallback;
import es.javocsoft.android.lib.toolbox.firebase.exception.FirebaseException;


/**
 * Firebase Cloud Messaging (FCM) module.
 *
 * <ul>
 *     <li>Registration
 *         <ul>
 *             <li>Receives Firebase registration id for the application and device.</li>
 *             <li>Execute any process that your backend can need.</li>
 *         </ul>
 *     </li>
 *     <li>Un-registration
 *         <ul>
 *             <li>Un-registers from Google Firebase.</li>
 *             <li>Execute any process that your backend can need.</li>
 *         </ul>
 *     </li>
 *     <li>Notification receive
 *         <ul>
 *             <li>Receives any notification sent to the application.</li>
 *             <li>Execute any process that your backend can need (ACK Received/Opened)</li>
 *         </ul>
 *     </li>
 *
 * </ul>
 *
 * See <a href="https://firebase.google.com/docs/android/setup?hl=es-419">Firebase</a> help. To test
 * the module use use <a href="https://console.firebase.google.com">Firebase Console</a> to deliver
 * some mesaages to your app.
 *
 * @author JavocSoft, 2017
 * @since  2017
 * @version 1.0
 *
 */
public class NotificationModule {

	/** Enables or disables the log. */
	public static boolean LOG_ENABLE = true;
	public static final String TAG = "JavocsoftToolbox:FCM";

	public static final String FIREBASE_PREF_NAME = "FIREBASE_PREFS";

	public static final String FIREBASE_PREF_KEY_REGID = "FIREBASE_PREF_REG_ID";
	public static final String FIREBASE_PREF_KEY_APP_VERSION = "FIREBASE_PREF_APP_VERSION";
	public static final String FIREBASE_PREF_KEY_REG_ERROR_CODE = "FIREBASE_PREF_REG_ERROR_CODE";
	public static final String FIREBASE_PREF_KEY_UNREG_ERROR_CODE = "FIREBASE_PREF_UNREG_ERROR_CODE";

	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_ACTIVITY_TO_CALL = "FIREBASE_PREF_APP_NOTIFICATION_ACTIVITY_TO_CALL";
	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_ONNOTRECEIVEDTHREAD_TO_CALL = "FIREBASE_PREF_APP_NOTIFICATION_ONNOTRECEIVEDTHREAD_TO_CALL";
	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_TITLE = "FIREBASE_PREF_APP_NOTIFICATION_TITLE";
	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_MULTIPLENOT = "FIREBASE_PREF_APP_NOTIFICATION_MULTIPLENOT";
	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_GROUPMULTIPLENOTKEY = "FIREBASE_PREF_APP_NOTIFICATION_GROUPMULTIPLENOTKEY";
	public static final String FIREBASE_PREF_KEY_APP_NOTIFICATION_APPPACKAGE = "FIREBASE_PREF_APP_NOTIFICATION_APPPACKAGE";

	private static NotificationModule instance = null;
	
	/** This will hold the Firebase device registration token. */
	public String deviceToken =null;
	
	/** Just in case the application owner wants some kind of feedback */
	public static final String MESSAGE_EFFICACY_KEY = "notificationId";
	/** The key where the notification type of the notification is */
	public static final String ANDROID_NOTIFICATION_STYLE_KEY = "notStyle";
	/** The key where the message of the notification is */
	public static final String ANDROID_NOTIFICATION_TITLE_KEY = "title";
	/** The key where the message of the notification is */
	public static final String ANDROID_NOTIFICATION_MESSAGE_KEY = "message";
	/** The key where the ticker of the notification is */
	public static final String ANDROID_NOTIFICATION_TICKER_KEY = "ticker";
	/** The key where the content info of the notification is */
    public static final String ANDROID_NOTIFICATION_CONTENT_INFO_KEY = "contentInfo";
    /** The key where the big style title of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_TITLE_KEY = "bsTitle";
    /** The key where the big style content of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_CONTENT_KEY = "bsContent";
    /** The key where the big style summary of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_SUMMARY_KEY = "bsSummary";
    /** The key where the big picture style image of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_IMAGE_KEY = "bsImage";
    /** The key where the big style line content of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_INBOX_CONTENT_KEY = "bsInboxStyleContent";
    /** The key where the inbox style line separator character of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_INBOX_LINE_SEPARATOR_KEY = "bsInboxStyleLineSeparator";
    /** The key where the large icon of the notification is */
    public static final String ANDROID_NOTIFICATION_BIG_STYLE_LARGE_ICON_KEY = "bslargeIcon";

	/* Default properties of a notification received by firebase */
	public static final String ANDROID_NOTIFICATION_FROM_KEY = "from";
	public static final String ANDROID_NOTIFICATION_COLLAPSEKEY_KEY = "collapseKey";
	public static final String ANDROID_NOTIFICATION_MESSAGEID_KEY = "messageId";
	public static final String ANDROID_NOTIFICATION_TO_KEY = "to";
	public static final String ANDROID_NOTIFICATION_SENTTIME_KEY = "sentTime";
	public static final String ANDROID_NOTIFICATION_TTLENABLED_KEY = "ttl";
	public static final String ANDROID_NOTIFICATION_CLICKACTION_KEY = "clickAction";
	public static final String ANDROID_NOTIFICATION_COLOR_KEY = "color";
	public static final String ANDROID_NOTIFICATION_ICON_KEY = "icon";
	public static final String ANDROID_NOTIFICATION_SOUND_KEY = "sound";
	public static final String ANDROID_NOTIFICATION_TAG_KEY = "tag";
	public static final String ANDROID_NOTIFICATION_TIT_KEY = "title";

		
	private final static String APP_NOTIFICATION_ACTION_KEY = "<app_package>";
	/** Custom intent used to show the alert in the UI about a received push. */
    public static String SHOW_NOTIFICATION_ACTION = "com.google.android.firebase."+ APP_NOTIFICATION_ACTION_KEY+".DISPLAY_MESSAGE";
    /** Used to do something in particular when a notification arrives */ 
    public static String NEW_NOTIFICATION_ACTION = "com.google.android.firebase."+ APP_NOTIFICATION_ACTION_KEY+".NEW_DISPLAY_MESSAGE";
    
	public static String NOTIFICATION_TITLE = null;
    public static Class<?> NOTIFICATION_ACTIVITY_TO_CALL = null;

    public static Context APPLICATION_CONTEXT;
    public static EnvironmentType ENVIRONMENT_TYPE = EnvironmentType.PRODUCTION;
    public static final String NOTIFICATION_ACTION_KEY = "NotificationActionKey";
    public static OnOpenAckCallback ackOpenRunnable;
    public static OnRegistrationCallback registerRunnable;
    public static OnUnregistrationCallback unregisterRunnable;
    public static OnNewNotificationCallback doWhenNotificationRunnable;
    
    public static boolean multipleNot;
    public static String groupMultipleNotKey;    
    public static String notBackgroundColor;
    public static boolean vibrate;

	AtomicInteger msgId = new AtomicInteger();

    ExecutorService executorService = Executors.newFixedThreadPool(1);
    
    
    protected NotificationModule(Context context, EnvironmentType environmentType,
                                 OnRegistrationCallback registerRunnable, OnOpenAckCallback ackRunnable, OnUnregistrationCallback unregisterRunnable,
                                 OnNewNotificationCallback doWhenNotificationRunnable,
                                 boolean multipleNot, String groupMultipleNotKey, String notBackgroundColor,
                                 boolean vibrate) {
		APPLICATION_CONTEXT = context;
		if(environmentType!=null){
			ENVIRONMENT_TYPE = environmentType;
		}

		NotificationModule.ackOpenRunnable = ackRunnable;
		NotificationModule.unregisterRunnable = unregisterRunnable;
		NotificationModule.registerRunnable = registerRunnable;
		NotificationModule.doWhenNotificationRunnable = doWhenNotificationRunnable;
		
		SHOW_NOTIFICATION_ACTION = SHOW_NOTIFICATION_ACTION.replaceAll(APP_NOTIFICATION_ACTION_KEY, APPLICATION_CONTEXT.getPackageName());
		NEW_NOTIFICATION_ACTION = NEW_NOTIFICATION_ACTION.replaceAll(APP_NOTIFICATION_ACTION_KEY, APPLICATION_CONTEXT.getPackageName());
		
		NotificationModule.multipleNot = multipleNot;
		NotificationModule.groupMultipleNotKey = groupMultipleNotKey;
		
		NotificationModule.notBackgroundColor = notBackgroundColor;
		NotificationModule.vibrate = vibrate;
		
		//Required, when application is closed, for the service that processes the notification 
		//to be able to create the notification for the app.
		ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_MULTIPLENOT, Boolean.class, NotificationModule.multipleNot);
		ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_GROUPMULTIPLENOTKEY, String.class, NotificationModule.groupMultipleNotKey);
        ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_ONNOTRECEIVEDTHREAD_TO_CALL, String.class, (doWhenNotificationRunnable!=null?NotificationModule.doWhenNotificationRunnable.getClass().getName():null));
		ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_APPPACKAGE, String.class, NotificationModule.APPLICATION_CONTEXT.getPackageName());
	}	
	
	
    /**
     * 
     * Initializes the Firebase notification module.
     * 
     * @param context							The application context.
     * @param environmentType					The environment type. See {@link EnvironmentType enumerator}.
     * @param registerRunnable					Optional. Something to do once the application registers with Firebase. See {@link OnRegistrationCallback}.
     * @param ackOpenRunnable						Optional. Something to do once the user opens a received push notification from Firebase. See {@link OnOpenAckCallback}.
     * @param unregisterRunnable				Optional. Something to do once the application un-registers from Firebase. See {@link OnUnregistrationCallback}.
     * @param doWhenNotificationRunnable		Optional. Something to do once the application receives a push notification from Firebase. See {@link OnNewNotificationCallback}.
     * @param multipleNot 						Setting to True allows showing multiple notifications.
	 * @param groupMultipleNotKey 				If is set, multiple notifications can be grupped by 
	 * 											this key.
	 * @param notBackgroundColor				Optional. Since Android 5.0+ notification icons must 
	 * 											follow a design guidelines to be showed correctly and allows to set the background 
	 * 											color for the icon. The specified color must be in hexadecimal, 
	 * 											for example "#ff6600".
	 * @param vibrate							Set to TRUE to enable vibration when notification arrives.
	 * 											Requires the permission android.permission.VIBRATE.
	 *
     * @return
     */
	public static NotificationModule getInstance(Context context, EnvironmentType environmentType,
                                                 OnRegistrationCallback registerRunnable,
												 OnOpenAckCallback ackOpenRunnable,
												 OnUnregistrationCallback unregisterRunnable,
                                                 OnNewNotificationCallback doWhenNotificationRunnable,
                                                 boolean multipleNot, String groupMultipleNotKey, String notBackgroundColor,
                                                 boolean vibrate, boolean enableLog) {

		if (instance == null) {
			LOG_ENABLE = enableLog;

			instance = new NotificationModule(context, environmentType,
						registerRunnable, ackOpenRunnable, unregisterRunnable,
						doWhenNotificationRunnable,
						multipleNot, groupMultipleNotKey, notBackgroundColor, vibrate);
		}
		return instance;
	}
	
	

	// NOTIFICATIONS - FIREBASE (INIT)
	
	/**
	 * Initializes the module for Firebase notifications.
	 * 
	 * <b>NOTE</b>:
	 * The environment is set by looking for the application debug mode,
	 * if is set to TRUE, the environment will be SANDBOX, otherwise PRODUCTION.
	 * 
	 * @param 	context		Context.  
	 * @param	title		Title for the notification
	 * @param 	clazz		Class to call when clicking in the notification
	 * @throws FirebaseException
	 */
	@SuppressWarnings("unused")
	public void firebaseInitializeModule(Context context, String title, Class<?> clazz) throws FirebaseException {
		
		EnvironmentType environment = ENVIRONMENT_TYPE;
		if(ToolBox.application_isAppInDebugMode(context)){
			environment = EnvironmentType.SANDBOX;
		}

		firebaseInitializeModule(context, environment, title, clazz);
	}

	/**
	 * Initializes the module for Firebase notification system.
	 * 
	 * @param 	context		Context.
	 * @param 	environment	Allows to set the environment type. 
	 * @param	title		Title for the notification
	 * @param 	clazz		Class to call when clicking in the notification
	 * @throws FirebaseException
	 */
	public void firebaseInitializeModule(final Context context, final EnvironmentType environment,
								  String title, Class<?> clazz) throws FirebaseException {
		
		ENVIRONMENT_TYPE = environment;
		NOTIFICATION_TITLE = title;
		NOTIFICATION_ACTIVITY_TO_CALL = clazz;
		
		//Required, when application is closed, for the service that processes the notification 
		//to be able to create the notification for the app.
		ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_ACTIVITY_TO_CALL, String.class, NotificationModule.NOTIFICATION_ACTIVITY_TO_CALL.getName());
		ToolBox.prefs_savePreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_APP_NOTIFICATION_TITLE, String.class, NotificationModule.NOTIFICATION_TITLE);

		Log.d(NotificationModule.TAG, "Firebase RegistrationId Token: " + getRegistrationId(context));
	}


	// UTILITY METHODS

	/**
	 * Returns the Google Cloud Messaging system in Firebase registration token.
	 * 
	 * @param context
	 * @return	The token or null if the device is not registered yet.
	 */
	@SuppressWarnings("unused")
	public String firebaseGetRegistrationToken(Context context){
		return getRegistrationId(context);		
	}
	
	/**
	 * Returns the Android device unique id.
	 * <br><br>
	 * TIP: Is recommended to require the permission "READ_PHONE_STATE".
	 * 
	 * @param context
	 * @return	The android device unique id.
	 */
	@SuppressWarnings("unused")
	public String firebaseGetDeviceUdid(Context context){
		return ToolBox.device_getId(context);
	}
	
	/**
	 * Unregister the device from Google Cloud Messaging system (Firebase)
	 * 
	 * @param context
	 */
	@SuppressWarnings("unused")
	public void firebaseUnregisterDevice(Context context){
		executorService.execute(new FirebaseUnRegistration(context));
	}

	/**
	 * Subscribes the application to the topic in Firebase.
	 *
	 * @param topic
	 */
	@SuppressWarnings("unused")
	public void subscribeToTopic(String topic){
		if(topic!=null && topic.length()>0)
			FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

	/**
	 * Unsubscribes the application from the topic in Firebase.
	 *
	 * @param topic
	 */
	@SuppressWarnings("unused")
	public void unsubscribeFromTopic(String topic){
		if(topic!=null && topic.length()>0)
			FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
	}


	// NOTIFICATIONS RECEIVAL

	/**
	 * Makes the UI show the alert for any received notification.
	 * 
	 * @param context
	 * @param intent
	 */
	@SuppressWarnings("unused")
	public void firebaseCheckForNotificationReceival(Context context, Intent intent){

		if(intent!=null && intent.getAction()!=null && NOTIFICATION_ACTIVITY_TO_CALL.getName()!=null && 
			intent.getAction().equals(NotificationModule.NOTIFICATION_ACTIVITY_TO_CALL.getName()+"."+ NotificationModule.NOTIFICATION_ACTION_KEY)){
			//The event is a PUSH notification from Firebase
			if(LOG_ENABLE)
				Log.d(NotificationModule.TAG, "onNewIntent - " + intent.getAction());
			
			if(intent.getExtras()!=null){
				//Tells the UI to launch the service to handle opening events
				// (using the BroadcastReceiver "NotificationOpenedReceiver".
				Intent intentOpenAlert = new Intent(NotificationModule.NEW_NOTIFICATION_ACTION);
				intentOpenAlert.setPackage(NotificationModule.APPLICATION_CONTEXT.getPackageName());
				intentOpenAlert.putExtras(intent.getExtras());

		        context.sendBroadcast(intentOpenAlert);

				if(LOG_ENABLE)
					Log.d(NotificationModule.TAG, "Notification received. Show order sent to " +
							"broadcast listening to [" + NotificationModule.NEW_NOTIFICATION_ACTION + "] action.");
			}
		}
	}

	
	//AUXILIAR


	/**
	 * Gets the current registration ID for application on Firebase service.
	 * <p>
	 * If result is null, the app is not yet registered with Firebase or did not get the token.
	 *
	 * @param context
	 * @return registration ID, or null string if there is no existing Firebase registration ID.
	 */
	public static String getRegistrationId(Context context) {

		String firebaseRegistrationId = (String)ToolBox.prefs_readPreference(context, FIREBASE_PREF_NAME, FIREBASE_PREF_KEY_REGID, String.class);
		if(firebaseRegistrationId==null) {
			firebaseRegistrationId = FirebaseInstanceId.getInstance().getToken();
			if(firebaseRegistrationId!=null){
				saveRegistrationId(context, firebaseRegistrationId);
			}else{
				//Not registered yet
				return null;
			}
		}

	    //Return the current registration id if is OK.
	    return firebaseRegistrationId;
	}

	/**
	 * Stores in shared preferences the current application version and the Firebase RegistrationId
	 * token.
	 *
	 * @param context
	 * @param token
	 */
	public static void saveRegistrationId(Context context, String token) {
		//Store registration Id
		if(!ToolBox.prefs_savePreference(context, NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_REGID, String.class, token)){
			Log.e(NotificationModule.TAG, "Could not save Firebase RegistrationId token " + token + " in shared preferences.");
		}else{
			if(NotificationModule.LOG_ENABLE)
				Log.d(NotificationModule.TAG, "Saved Firebase RegistrationId token " + token + " in shared preferences.");
		}

		//Save the application version for the Firebase registrationId.
		int currentVersion = ToolBox.application_getVersionCode(context.getApplicationContext());
		if(!ToolBox.prefs_savePreference(context, NotificationModule.FIREBASE_PREF_NAME, NotificationModule.FIREBASE_PREF_KEY_APP_VERSION, Integer.class, currentVersion)){
			Log.e(NotificationModule.TAG, "Application version code " + currentVersion + " could not be saved in the shared preferences.");
		}else{
			if(NotificationModule.LOG_ENABLE)
				Log.d(NotificationModule.TAG, "Application version code " + currentVersion + " saved in the shared preferences.");
		}
	}

}
