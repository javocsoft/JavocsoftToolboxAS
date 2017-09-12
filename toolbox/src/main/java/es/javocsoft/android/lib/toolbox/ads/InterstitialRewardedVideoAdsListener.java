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
package es.javocsoft.android.lib.toolbox.ads;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.gcm.NotificationModule;

/**
 * Google AdMob interstitial lifecycle listener.
 *
 * @author JavocSoft 2014
 * @version 1.0
 */
public class InterstitialRewardedVideoAdsListener implements RewardedVideoAdListener {

    private RewardedVideoAd adView;
    private OnRewardedVideoInterstitialClickCallback clickCallback;
    private OnRewardedVideoInterstitialRewardCallback rewardCallback;

    public InterstitialRewardedVideoAdsListener(RewardedVideoAd adView, OnRewardedVideoInterstitialClickCallback clickCallback,
                                                OnRewardedVideoInterstitialRewardCallback rewardCallback) {
        this.adView = adView;
        this.clickCallback = clickCallback;
        this.rewardCallback = rewardCallback;
    }

    /** Gets a string error reason from an error code. */
    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch(errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        return errorReason;
    }

    /** Called when a rewarded video ad is loaded. */
    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The rewarded video was loaded.");
        adView.show();
    }

    /** Called when a rewarded video ad opens a overlay that covers the screen. */
    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The rewarded video was opened.");
    }

    /** Called when a rewarded video ad starts to play. */
    @Override
    public void onRewardedVideoStarted() {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The video was started.");
    }

    /** Called when a rewarded video ad is closed. */
    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The video was closed.");
    }

    /**
     * Called when a rewarded video ad has triggered a reward. The app is responsible for
     * crediting the user with the reward.
     * Notes: The reward to grant the user. This value will never be null. If the ad does not
     * specify a reward amount and no override is provided for this ad unit on the Admob UI the
     * default reward will have an amount of 1 and a type of "".
     * */
    @Override
    public void onRewarded(RewardItem rewardItem) {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The video generated a reward (type: " + rewardItem.getType() + "/amount: " + rewardItem.getAmount());
        if(rewardCallback!=null){
            rewardCallback.rewardItem = rewardItem;
            rewardCallback.start();
        }
    }

    /**
     * Called when a rewarded video ad leaves the application (e.g., to go to the browser).
     * (when is clicked and going to start a new Activity that will
     * leave the application (e.g. breaking out to the Browser or Maps
     * application).
     */
    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(ToolBox.TAG, "AdsRewardedVideo: The video was clicked and application is going to be put in background.");
        if(clickCallback!=null)
            clickCallback.start();
    }

    /** Called when a rewarded video ad request failed. */
    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        String message = "onRewardedVideoAdFailedToLoad: " + getErrorReason(errorCode);
        Log.d(ToolBox.TAG, "Ads: Error loading ads [" + message + "].");
    }


    /**
	 * This class allows to do something when an user clicks
	 * on a rewarded video interstitial.
	 * 
	 * @author JavocSoft 2013.
	 * @since 2014
	 */
	public static abstract class OnRewardedVideoInterstitialClickCallback extends Thread implements Runnable {
		
		
		protected OnRewardedVideoInterstitialClickCallback() {}
		
		@Override
		public void run() {
			pre_task();
			task();
			post_task();
		}
		    	
		protected abstract void pre_task();
		protected abstract void task();
		protected abstract void post_task();
			
		
		/**
		 * Gets the context.
		 * 
		 * @return
		 */
		protected Context getContext(){
			return NotificationModule.APPLICATION_CONTEXT;
		}
	}

    /**
     * This class allows to do something with the received reward of a rewarded video interstitial.
     *
     * @author JavocSoft 2017.
     * @since 2014
     */
    public static abstract class OnRewardedVideoInterstitialRewardCallback extends Thread implements Runnable {

        protected RewardItem rewardItem;

        protected OnRewardedVideoInterstitialRewardCallback() {}

        @Override
        public void run() {
            pre_task();
            task();
            post_task();
        }

        protected abstract void pre_task();
        protected abstract void task();
        protected abstract void post_task();


        /**
         * Gets the context.
         *
         * @return
         */
        protected Context getContext(){
            return NotificationModule.APPLICATION_CONTEXT;
        }
    }
}
