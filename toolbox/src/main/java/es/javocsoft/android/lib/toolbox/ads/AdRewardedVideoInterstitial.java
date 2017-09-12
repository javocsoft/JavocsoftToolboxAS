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

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import es.javocsoft.android.lib.toolbox.ToolBox;

/**
 * This class allows to manage interstitial ads from Google AdMob.
 *
 * See documentation for more information about the usage.
 *
 * @author JavocSoft 2014
 * @version 1.0
 */
public class AdRewardedVideoInterstitial extends AdBase{

    private static AdRewardedVideoInterstitial thiz = null;


    public static AdRewardedVideoInterstitial getInstance() {
        if(thiz==null)
            thiz = new AdRewardedVideoInterstitial();

        return thiz;
    }


    /**
     * Shows a new interstitial.
     *
     * @param admobAdID    Your AdMob interstitial id.
     * @param activity
     * @param clickCallback See {@link InterstitialRewardedVideoAdsListener.OnRewardedVideoInterstitialClickCallback}
     * @param rewardCallback See {@link InterstitialRewardedVideoAdsListener.OnRewardedVideoInterstitialRewardCallback}
     * @return
     */
    public RewardedVideoAd requestInterstitial(String admobAdID, Activity activity,
                                              InterstitialRewardedVideoAdsListener.OnRewardedVideoInterstitialClickCallback clickCallback,
                                              InterstitialRewardedVideoAdsListener.OnRewardedVideoInterstitialRewardCallback rewardCallback) {
        Log.i(ToolBox.TAG, "Ads: Preparing a new rewarded video interstitial.");

        // Create the interstitial.
        RewardedVideoAd mAdRewardedVideo = MobileAds.getRewardedVideoAdInstance(activity);
        AdRequest adRequest = interstitialRewardedVideoAdRequest(mAdRewardedVideo, activity);
        if(adRequest!=null) {
            mAdRewardedVideo.loadAd(admobAdID, adRequest);
            mAdRewardedVideo.setRewardedVideoAdListener(new InterstitialRewardedVideoAdsListener(mAdRewardedVideo, clickCallback, rewardCallback));
        }

        return mAdRewardedVideo;
    }
}
