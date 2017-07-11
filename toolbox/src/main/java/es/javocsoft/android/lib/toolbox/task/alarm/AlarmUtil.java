package es.javocsoft.android.lib.toolbox.task.alarm;

import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Allows to manage alarm clock tasks.
 * 
 * @author JavocSoft 2017
 * @since  2017
 * 
 */
public class AlarmUtil {

	public AlarmUtil() {
		
	}
	
	
	/**
	 * 
	 * @param context
	 * @param delayInMillis
	 * @param alarmReceiver
	 * @param alarmRequestCode
	 * @param action
	 */
	public static void schedule(Context context, long delayInMillis, AlarmReceiverHelper alarmReceiver, int alarmRequestCode, String action){
		// The time at which the alarm will be scheduled. Here the alarm is scheduled for 1 day from the current time. 
	    // We fetch the current time in milliseconds and add 1 day's time
	    // i.e. 24*60*60*1000 = 86,400,000 milliseconds in a day.       
	    Long time = new GregorianCalendar().getTimeInMillis()+delayInMillis;

	    // Get the intent
	    PendingIntent pIntent = createPendingIntent(context, alarmReceiver, alarmRequestCode, action);
	    
	    // Get the Alarm Service.
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

	    // Set the alarm for a particular time.
	    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
	}
	
	/**
	 * 
	 * @param context
	 * @param delay
	 * @param repeatAtInterval
	 * @param alarmReceiver
	 * @param alarmRequestCode
	 * @param action
	 */
	public static void scheduleAndRepeat(Context context, long delay, long repeatAtInterval, AlarmReceiverHelper alarmReceiver, int alarmRequestCode, String action){
		// The time at which the alarm will be scheduled. Here the alarm is scheduled for 1 day from the current time. 
	    // We fetch the current time in milliseconds and add 1 day's time
	    // i.e. 24*60*60*1000 = 86,400,000 milliseconds in a day.       
	    Long time = new GregorianCalendar().getTimeInMillis()+delay;

	    // Get the intent
	    PendingIntent pIntent = createPendingIntent(context, alarmReceiver, alarmRequestCode, action);
	    
	    // Get the Alarm Service.
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

	    // Set the alarm for a particular time and repeats.
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, repeatAtInterval, pIntent);
	}
	
	
	/**
	 * 
	 * @param context
	 * @param alarmReceiver
	 * @param alarmRequestCode
	 * @param action
	 * @return
	 */
	private static PendingIntent createPendingIntent(Context context, AlarmReceiverHelper alarmReceiver, int alarmRequestCode, String action) {
		// Create an Intent and set the class that will execute when the Alarm triggers. Here we have
	    // specified AlarmReceiver in the Intent. The onReceive() method of this class will execute 
	    // when the broadcast from your alarm is received.
	    Intent intentAlarm = null;
	    if(alarmReceiver!=null){
	    	intentAlarm = new Intent(context, alarmReceiver.getClass());
	    	if(action!=null)
		    	intentAlarm.setAction(context.getPackageName() + "." + action);
	    }else{
	    	intentAlarm = new Intent(context.getPackageName() + "." + action);
	    }
	    
	    PendingIntent pIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    return pIntent;
	}
	
	/**
	 * 
	 * @param context
	 * @param alarmReceiver
	 * @param alarmRequestCode
	 * @param action
	 * @return
	 */
	public static boolean checkIfAlarmIsSet(Context context, AlarmReceiverHelper alarmReceiver, int alarmRequestCode, String action){
		
		Intent intentAlarm = null;
		if(alarmReceiver!=null) {
			intentAlarm = new Intent(context, alarmReceiver.getClass());
			if(action!=null)
		    	intentAlarm.setAction(context.getPackageName() + "." + action);
		}else{
			intentAlarm = new Intent(context.getPackageName() + "." + action);			
		}
		
		PendingIntent pIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intentAlarm , PendingIntent.FLAG_NO_CREATE);
		
		return (pIntent != null);
	}
	
	/**
	 * 
	 * @param context
	 * @param alarmReceiver
	 * @param alarmRequestCode
	 * @param action
	 */
	public static void CancelAlarm(Context context, AlarmReceiverHelper alarmReceiver, int alarmRequestCode, String action) {
		Intent intentAlarm = null;
		if(alarmReceiver!=null) {
			intentAlarm = new Intent(context, alarmReceiver.getClass());
			if(action!=null)
		    	intentAlarm.setAction(context.getPackageName() + "." + action);
		}else{
			intentAlarm = new Intent(context.getPackageName() + "." + action);			
		}
		
        PendingIntent pIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intentAlarm, PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);        
    }
}
