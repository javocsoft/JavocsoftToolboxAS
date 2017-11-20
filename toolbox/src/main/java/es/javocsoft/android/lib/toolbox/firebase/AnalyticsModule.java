package es.javocsoft.android.lib.toolbox.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * This class enables the use of Firebase Analytics in your application.
 * <br><br>
 * You can enable the detailed registry with some ADB commands:
 * <br><br>
 *  adb shell setprop log.tag.FA VERBOSE<br>
 *  adb shell setprop log.tag.FA-SVC VERBOSE<br>
 *  adb logcat -v time -s FA FA-SVC<br>
 * <br>
 * These commands will show the events in your logcat console in Android Studio, this will help you
 * to verify that all events are sent correctly. This module needs to add in the application gradle
 * module "compile 'com.google.firebase:firebase-core:11.0.2'".
 * <br><br>
 * See <a href="https://firebase.google.com/docs/android/setup?hl=es-419">Firebase in your project</a>
 * to enable usage of Firebase in your application and
 * <a href="https://firebase.google.com/docs/analytics/android/start/?hl=es-419">Firebase AnalyticsModule</a>
 * to know more about AnalyticsModule usage in Firebase.
 *
 * @author JavocSoft, 2017
 * @since 2017
 * @version 1.0.0
 */
@SuppressWarnings({"unused"})
public class AnalyticsModule {

    private static FirebaseAnalytics mFirebaseAnalytics;

    protected AnalyticsModule(){}

    /**
     * Initializes the Firebase Analytics Module.
     * <br><br>
     * Notes:<br>
     * Remember that you have to set in the AndroidManifest.xml the permissions:
     * <ul>
     *     <li>android.permission.INTERNET</li>
     *     <li>android.permission.ACCESS_NETWORK_STATE</li>
     *     <li>android.permission.WAKE_LOCK</li>
     * </ul>
     *
     * @param context The context
     */
    @SuppressWarnings({"MissingPermission"})
    public AnalyticsModule(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }


    //METHODS

    /**
     * Logs an application opened event into Firebase AnalyticsModule.
     */
    public void logOpenAppEvent() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    /**
     *
     * @param id    The id of the selected element
     * @param name  The name of the selected element
     * @param type  The type of the selected element
     */
    public void logSelectContentEvent(String id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * Logs an application screen viewed event into Firebase AnalyticsModule.
     *
     * @param screenName    The screen name.
     */
    public void logScreenEvent(String screenName) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "screen");
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, screenName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
    }

    /**
     * Logs an application custom event into Firebase AnalyticsModule.
     *
     * @param eventName Optional. The event name. If null, "ga_event" is used.
     * @param category  The custom category of the event.
     * @param action    The custom action of the event.
     * @param label     The custom label for the event.
     * @param value     Optional. A value for the event.
     */
    public void logEvent(String eventName, String category, String action, String label, Long value) {
        Bundle params = new Bundle();
        params.putString("category", category);
        params.putString("action", action);
        params.putString("label", label);
        if(value!=null)
            params.putLong("value", value);

        mFirebaseAnalytics.logEvent((eventName!=null?eventName:"ga_event"), params);
    }

}
