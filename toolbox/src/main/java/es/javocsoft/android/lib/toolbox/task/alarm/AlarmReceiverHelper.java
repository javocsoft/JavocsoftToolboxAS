package es.javocsoft.android.lib.toolbox.task.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Implement doTask and 
 */
public abstract class AlarmReceiverHelper extends BroadcastReceiver{
	
	public abstract void doTask(Context context, Intent intent);
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Your code to execute when the alarm triggers and the broadcast is received.
    	doTask(context, intent);
    }
}
