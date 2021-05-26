/*
 *    Copyright 2013 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk.mediatedviews;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import com.appnexus.opensdk.MediatedInterstitialAdView;
import com.appnexus.opensdk.MediatedInterstitialAdViewController;
import com.appnexus.opensdk.TargetingParameters;
import com.appnexus.opensdk.utils.StringUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import java.util.ArrayList;

/**
 * This class is the Google AdMob interstitial adaptor it provides the functionality needed to allow
 * an application using the App Nexus SDK to load a banner ad through the Google SDK. The instantiation
 * of this class is done in response from the AppNexus server for a banner placement that is configured
 * to use AdMob to serve it. This class is never instantiated by the developer.
 * <p/>
 * This class also serves as an example of how to write a Mediation adaptor for the AppNexus
 * SDK.
 */
public class GooglePlayServicesInterstitial implements MediatedInterstitialAdView {
    private InterstitialAd interstitialAd;
    private GooglePlayAdListener adListener;

    @Override
    public void requestAd(MediatedInterstitialAdViewController mIC, Activity activity,
                          String parameter, String adUnitId, TargetingParameters targetingParameters) {
        adListener = new GooglePlayAdListener(mIC, super.getClass().getSimpleName());
        adListener.printToClog(String.format(" - requesting an ad: [%s, %s]", parameter, adUnitId));

        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(adUnitId);
        interstitialAd.setAdListener(adListener);

        try {
            interstitialAd.loadAd(buildRequest(targetingParameters));
        } catch (NoClassDefFoundError e) {
            // This can be thrown by Play Services on Honeycomb.
            adListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
        }
    }

    @Override
    public void show() {
        adListener.printToClog("show called");
        if (interstitialAd == null) {
            adListener.printToClogError("show called while interstitial ad view was null");
            return;
        }
        if (!interstitialAd.isLoaded()) {
            adListener.printToClogError("show called while interstitial ad view was not ready");
            return;
        }

        interstitialAd.show();
        adListener.printToClog("interstitial ad shown");
    }

    @Override
    public boolean isReady() {
        return (interstitialAd != null) && (interstitialAd.isLoaded());
    }

    private AdRequest buildRequest(TargetingParameters targetingParameters) {
        AdRequest.Builder builder = new AdRequest.Builder();

        Bundle bundle = new Bundle();

        if (!StringUtil.isEmpty(targetingParameters.getAge())) {
            bundle.putString("Age", targetingParameters.getAge());
        }
        if (targetingParameters.getLocation() != null) {
            builder.setLocation(targetingParameters.getLocation());
        }
        for (Pair<String, String> p : targetingParameters.getCustomKeywords()) {
            if (p.first.equals("content_url")) {
                if (!StringUtil.isEmpty(p.second)) {
                    builder.setContentUrl(p.second);
                }
            } else {
                if (bundle.containsKey(p.first)) {
                    ArrayList<String> listValues = new ArrayList<>();
                    Object value = bundle.get(p.first);
                    if (value instanceof String) {
                        listValues.add((String) value);
                    } else if (value instanceof ArrayList) {
                        listValues.addAll((ArrayList<String>) value);
                    }
                    listValues.add(p.second);
                    bundle.putStringArrayList(p.first, listValues);
                } else {
                    bundle.putString(p.first, p.second);
                }
            }
        }

        //Since AdMobExtras is deprecated so we need to use below method
        builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, bundle);

        return builder.build();
    }

    @Override
    public void destroy() {
        if(interstitialAd!=null){
            interstitialAd.setAdListener(null);
            interstitialAd=null;
            adListener=null;
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        destroy();
    }
}
