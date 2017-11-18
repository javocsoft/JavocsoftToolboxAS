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
package es.javocsoft.android.lib.toolbox.firebase.send;

import com.google.gson.Gson;

import org.apache.http.params.CoreProtocolPNames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.javocsoft.android.lib.toolbox.firebase.core.beans.FirebaseDeliveryResponse;
import es.javocsoft.android.lib.toolbox.firebase.core.beans.FirebaseDeliveryResultItem;
import es.javocsoft.android.lib.toolbox.firebase.core.beans.FirebaseMessage;
import es.javocsoft.android.lib.toolbox.net.HttpOperations;


/**
 * HTTP Firebase server implementation.
 * 
 * Firebase server side delivery helper class. You can export this class onto
 * a running Java project or you can use it within an Android app to
 * be able to deliver PUSH notification inside the app.
 * 
 * A message to a Firebase server has the format:
 * 
 * <pre>
 * { "collapse_key": "score_update",
 *   "time_to_live": 108,
 *   "delay_while_idle": true,
 *   "data": {
 *     "score": "4x8",
 *     "time": "15:16.2342"
 *   },
 *   "registration_ids":["4", "8", "15", "16", "23", "42"]
 * }
 * </pre>
 * 
 * @author JavocSoft, 2017
 * @since  2017
 *
 */
@SuppressWarnings("unused")
public class FCMHttpDelivery {

	
	private static final String FCM_HTTP_HEADER_AUTH_KEY = "Authorization";
	private static final String FCM_HTTP_HEADER_CONTENTTYPE_KEY = "Content-Type";
	private static final String FCM_HTTP_HEADER_CONTENTTYPE_VALUE = "application/json";
	private static final String FCM_HTTP_ENDPOINT = "https://fcm.googleapis.com/fcm/send";

	
	private static final Gson gson = new Gson();
	
	
	
	/**
	 * Sends a FCM PUSH message to a device.
	 * 
	 * See: https://firebase.google.com/docs/cloud-messaging/server
	 * 
	 * @param apiKey			The required API Key from Google Cloud Console FCM API.
	 * @param data				The data to send. A bunch of key-data pairs, up to 4k of data.
	 * @param collapseKey		If set, if two messages have the same collapse key, the last will override the
	 * 							previous one so there will be always only the last message of that collapse key.
	 * 							This kind of messages are like a "ping" and are also called "Send-to-sync messages". 
	 * 							FCM allows a maximum of 4 different collapse keys so, there can be 4 diferent kind of
	 * 							"Send-to-sync messages" per each application/device. If a collapse key is not set, the
	 * 							limit is currently 100. 
	 * @param delayWhileIdle	If set to TRUE, the message will be only delivered if the destination device is not idle.
	 * 							The time a message is stored in FCM servers by default is 4 weeks, after that time, the
	 * 							message is deleted.
	 * @param timeToLive		Setting this value overrides the default time that a message is stored in the FCM servers (4 weeks).
	 * 							The value are seconds where the maximum value is 2419200 seconds = 28 days. When setting 
	 * 							a of 0 seconds FCM will guarantee best effort for messages that must be delivered "now or never".
	 * 							Keep in mind that a "time_to_live" value of 0 means messages that can't be delivered immediately 
	 * 							will be discarded. However, because such messages are never stored, this provides the best latency 
	 * 							for sending notifications.
	 * @param devices			The device list (FCM registration id list).
	 * @param retries			Number of retries, default is 5.
	 * @return FirebaseDeliveryResponse
	 * @throws FCMDeliveryException
	 */
	public static FirebaseDeliveryResponse fcm_sendMessageToDevice(String apiKey, Map<String, String> data,
																   String collapseKey, boolean delayWhileIdle, Long timeToLive,
																   List<String> devices, int retries) throws FCMDeliveryException{
		
		String json = message_prepare(collapseKey, delayWhileIdle, timeToLive, data, devices);
		return fcm_sendMessage(json, apiKey, retries);
	}
	
	
	//AUXILIAR
	
	/* 
	 * Prepares a FCM JSON string to be sent in a POST request to FCM servers,
	 * 
	 * @param collapseKey		If set, if two messages have the same collapse key, the last will override the
	 * 							previous one so there will be always only the last message of that collapse key.
	 * 							This kind of messages are like a "ping" and are also called "Send-to-sync messages". 
	 * 							FCM allows a maximum of 4 different collapse keys so, there can be 4 diferent kind of
	 * 							"Send-to-sync messages" per each application/device. If a collapse key is not set, the
	 * 							limit is currently 100.
	 * @param delayWhileIdle	If set to TRUE, the message will be only delivered if the destination device is not idle.
	 * 							The time a message is stored in FCM servers by default is 4 weeks, after that time, the
	 * 							message is deleted.
	 * @param timeToLive		Setting this value overrides the default time that a message is stored in the FCM servers (4 weeks).
	 * 							The value are seconds where the maximum value is 2419200 seconds = 28 days. When setting 
	 * 							a of 0 seconds FCM will guarantee best effort for messages that must be delivered "now or never".
	 * 							Keep in mind that a "time_to_live" value of 0 means messages that can't be delivered immediately 
	 * 							will be discarded. However, because such messages are never stored, this provides the best latency 
	 * 							for sending notifications.
	 * @param data				The data to send. A bunch of key-data pairs, up to 4k of data.
	 * @param devices			The device list (FCM registration id list).
	 * @return
	 */
	private static String message_prepare(String collapseKey, boolean delayWhileIdle, 
										  Long timeToLive, Map<String, String> data,
										  List<String> devices) {
		
		String json = null;
		
		FirebaseMessage msg = new FirebaseMessage();
		
		msg.delay_while_idle = delayWhileIdle;
		msg.registration_ids = devices;
		
		if(data!=null && data.size()>0)
			msg.data = data;		
		if(collapseKey!=null && collapseKey.length()>0)
			msg.collapse_key = collapseKey;
        if(timeToLive>=0)
        	msg.time_to_live = timeToLive;
                
        json = gson.toJson(msg);
        
        return json;
	}
	
	
	/*
	 * Sends a JSON delivery string through FCM servers.
	 * 
	 * @param jsonData	The FCM JSON prepared string data.
	 * @param apiKey	The required API Key from Google Cloud Console FCM API.
	 */
	private static FirebaseDeliveryResponse fcm_sendMessage(String jsonData, String apiKey, int retries) throws FCMDeliveryException {
		
		FirebaseDeliveryResponse response = null;
		
		//Prepare the headers
		Map<String, String> headersData = new HashMap<String,String>();
		headersData.put(FCM_HTTP_HEADER_AUTH_KEY, "key="+apiKey);
		headersData.put(FCM_HTTP_HEADER_CONTENTTYPE_KEY, FCM_HTTP_HEADER_CONTENTTYPE_VALUE);
		headersData.put(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		
		if(retries<=0){
			retries = 5; //Default
		}
		
		for(int i=0; i<retries;i++){
			try {
				//Send the request to FCM servers.
				String responseRaw = HttpOperations.doPost(FCM_HTTP_ENDPOINT, jsonData, headersData);
				//Get the response object
				response = gson.fromJson(responseRaw, FirebaseDeliveryResponse.class);
				break; //Successfully submitted and get response
				
			} catch (Exception e) {}
		}
		
		if(response==null) {			 
			throw new FCMDeliveryException("Push delivery could not be done. Retries consumed.");
		}
				
		return response;
	}

	
	/**
	 * Used for FCM push deliveries exceptions.
	 * 
	 *  @author JavocSoft 2017
	 *	@version 1.0
	 */
	@SuppressWarnings("serial")
	public static class FCMDeliveryException extends Exception {
		
		public FCMDeliveryException() {
			super();			
		}

		public FCMDeliveryException(String message, Throwable cause) {
			super(message, cause);			
		}

		public FCMDeliveryException(String message) {
			super(message);			
		}

		public FCMDeliveryException(Throwable cause) {
			super(cause);			
		}
	}
	
	
	// TESTING HOWTO
	
	/*
	public static void main(String[] args) {
		
		//NOTE: These tests should be done within a
		//normal Java project, not an Android project.
		
		//Testing creation of FCM JSON request and
		//parse response.
		testingDelivery1();
		
		//Real FCM delivery test
		testingDelivery2();
	}*/
	
	
	@SuppressWarnings("unused")
	private static void testingDelivery1 () {
		//Text of FCM JSON creation:
		FirebaseMessage msg = new FirebaseMessage();
		msg.collapse_key = "score_update";
		msg.time_to_live = 108l;
		msg.delay_while_idle = true;
		msg.registration_ids = Arrays.asList(new String[]{"1","2","3","4"});
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("message", "Some message");
		msg.data = data;
		
		System.out.println(gson.toJson(msg));
		
		
		//Test of FCM JSON response parsing:
		String responseSimulated = "{ \"multicast_id\": \"216\",\"success\": 3,\"failure\": 3,\"canonical_ids\": 1,\"results\": [{ \"message_id\": \"1:0408\" },{ \"error\": \"Unavailable\" },{ \"error\": \"InvalidRegistration\" },{ \"message_id\": \"1:1516\" },{ \"message_id\": \"1:2342\", \"registration_id\": \"32\" },{ \"error\": \"NotRegistered\"}]}";
		FirebaseDeliveryResponse responseObj = gson.fromJson(responseSimulated, FirebaseDeliveryResponse.class);
		System.out.println(responseObj.success);
		System.out.println(responseObj.failure);
		System.out.println(responseObj.canonical_ids);
		
		for(FirebaseDeliveryResultItem resultItem:responseObj.results){
			System.out.println("ResultItem-> [" + 
									(resultItem.message_id!=null?resultItem.message_id:"") + "/" +										
									(resultItem.registration_id!=null?resultItem.registration_id:"") + "/" +											
									(resultItem.error!=null?resultItem.error:"") + "]"
								);
		}
	}
	
	@SuppressWarnings("unused")
	private static void testingDelivery2 () {
		final String MYPHONE_FCM_REGID = "some_reg_id";
		final String FCM_SERVER_KEY_TO_SEND = "some_key";
		
		FirebaseDeliveryResponse responseObj = null;
		try {
			//NOTE: This must be the key to use to store the notification message
			final String FCM_DELIVERY_MESSAGE_KEY = "message";
			
			final String FCM_DELIVERY_COLLAPSE_KEY = "collapse";
			final int FCM_DELIVERY_RETRIES = 3;
									
			Map<String, String> data = new HashMap<String, String>();
			data.put(FCM_DELIVERY_MESSAGE_KEY, "A notification message");
			
			responseObj = fcm_sendMessageToDevice(FCM_SERVER_KEY_TO_SEND, data, FCM_DELIVERY_COLLAPSE_KEY, false, -1l,
								Arrays.asList(new String[]{MYPHONE_FCM_REGID}),
								FCM_DELIVERY_RETRIES);
			
			System.out.println(responseObj.success);
			System.out.println(responseObj.failure);
			System.out.println(responseObj.canonical_ids);
			
			for(FirebaseDeliveryResultItem resultItem:responseObj.results){
				System.out.println("ResultItem-> [" + 
										(resultItem.message_id!=null?resultItem.message_id:"") + "/" +										
										(resultItem.registration_id!=null?resultItem.registration_id:"") + "/" +											
										(resultItem.error!=null?resultItem.error:"") + "]"
									);
			}
			
		} catch (FCMDeliveryException e) {
			e.printStackTrace();
		}
	}
}
