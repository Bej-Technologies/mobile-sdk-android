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

package com.appnexus.opensdk;

import com.appnexus.opensdk.mocks.MockDefaultExecutorSupplier;
import com.appnexus.opensdk.shadows.ShadowAsyncTaskNoExecutor;
import com.appnexus.opensdk.shadows.ShadowCustomWebView;
import com.appnexus.opensdk.shadows.ShadowSettings;
import com.appnexus.opensdk.shadows.ShadowWebSettings;
import com.squareup.okhttp.mockwebserver.MockResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

@Config(sdk = 21,
        shadows = {ShadowAsyncTaskNoExecutor.class,
                ShadowCustomWebView.class, ShadowWebSettings.class, ShadowSettings.class, ShadowLog.class})
@RunWith(RobolectricTestRunner.class)
public class AdListenerTest extends BaseViewAdTest {

    @Override
    public void setup() {
        super.setup();
    }

    //This verifies that the AsyncTask for Request is being executed on the Correct Executor.
    @Test
    public void testRequestExecutorForBackgroundTasks() {
        SDKSettings.setExternalExecutor(MockDefaultExecutorSupplier.getInstance().forBackgroundTasks());
        assertNotSame(ShadowAsyncTaskNoExecutor.getExecutor(), MockDefaultExecutorSupplier.getInstance().forBackgroundTasks());
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        assertEquals(ShadowAsyncTaskNoExecutor.getExecutor(), MockDefaultExecutorSupplier.getInstance().forBackgroundTasks());
    }

    // Banner Testing

    @Test
    public void testBannerAdLoaded() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.banner()));
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertCallbacks(true);
    }

    @Test
    public void testBannerAdFailed() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.blank()));
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertCallbacks(false);
    }

    // Interstitial Testing

    @Test
    public void testInterstitialAdLoaded() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.banner()));
        requestManager = new AdViewRequestManager(interstitialAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertCallbacks(true);
    }

    @Test
    public void testInterstitialAdFailed() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.blank()));
        requestManager = new AdViewRequestManager(interstitialAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertCallbacks(false);
    }

    //Banner-Native test
    @Test
    public void testBannerNativeAdLoaded() {
        bannerAdView.setAutoRefreshInterval(30000);
        bannerAdView.setLoadsInBackground(false);
        bannerAdView.setOpensNativeBrowser(false);
        bannerAdView.setClickThroughAction(ANClickThroughAction.RETURN_URL);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.anNativeWithoutImages()));
        Assert.assertEquals(AdType.UNKNOWN, bannerAdView.getAdType());
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertEquals(30000, bannerAdView.getAutoRefreshInterval());
        Assert.assertEquals(AdType.NATIVE, bannerAdView.getAdType());
        assertCallbacks(true);
        assertOpensInNativeBrowser();
        assertLoadsInBackground();
        assertClickThroughAction();
        assertClickThroughAction(ANClickThroughAction.RETURN_URL);
    }

    @Test
    public void testClickThroughDependencyOnOpensNativeFalse() {
        bannerAdView.setClickThroughAction(ANClickThroughAction.RETURN_URL);
        bannerAdView.setOpensNativeBrowser(false);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.anNativeWithoutImages()));
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertClickThroughAction();
        assertClickThroughAction(ANClickThroughAction.OPEN_SDK_BROWSER);
    }

    @Test
    public void testClickThroughDependencyOnOpensNativeTrue() {
        bannerAdView.setClickThroughAction(ANClickThroughAction.RETURN_URL);
        bannerAdView.setOpensNativeBrowser(true);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.anNativeWithoutImages()));
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertClickThroughAction();
        assertClickThroughAction(ANClickThroughAction.OPEN_DEVICE_BROWSER);
    }

    @Test
    public void testBannerNativeAdFailed() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.blank()));
        requestManager = new AdViewRequestManager(bannerAdView);
        requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertCallbacks(false);
    }
}
