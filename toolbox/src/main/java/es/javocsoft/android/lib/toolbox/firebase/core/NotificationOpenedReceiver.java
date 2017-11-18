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
package es.javocsoft.android.lib.toolbox.firebase.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;


/**
 * Used to receive when a notification is opened from the system bar
 * in the application UI. Only responds to the action
 * {@link NotificationModule#NEW_NOTIFICATION_ACTION}.
 *
 * Firebase STEP 3 - Notification opened (ACK Notification opened)
 *
 * <br><br>
 * Firebase Notification Messaging (FCM) module.
 *
 * @author JavocSoft, 2017
 * @since  2017
 */
public class NotificationOpenedReceiver extends BroadcastReceiver {

	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(NotificationModule.NEW_NOTIFICATION_ACTION)){
			if(NotificationModule.LOG_ENABLE)
				Log.i(NotificationModule.TAG,"NotificationOpenedReceiver. A received notification has been opened.");
				
			//Do something when notification is opened.
	        if(NotificationModule.ackOpenRunnable!=null &&
	           !NotificationModule.ackOpenRunnable.isAlive()){
	        	
	        	//Set the intent extras
				NotificationModule.ackOpenRunnable.setNotificationBundle(intent.getExtras());
	        	Thread tAck = new Thread(NotificationModule.ackOpenRunnable);
	        	tAck.start();

				if(NotificationModule.LOG_ENABLE)
					Log.i(NotificationModule.TAG, "ACK (open) executed.");
	        }
		}
	}
	
	
	//AUXILIAR CLASSES
	

}
