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

import android.os.AsyncTask;
import android.os.Build;
import com.appnexus.opensdk.utils.Clog;
import com.appnexus.opensdk.utils.HTTPGet;
import com.appnexus.opensdk.utils.HTTPResponse;

public abstract class MediatedAdViewController implements Displayable {

    public static enum RESULT {
        SUCCESS,
        INVALID_REQUEST,
        UNABLE_TO_FILL,
        MEDIATED_SDK_UNAVAILABLE,
        NETWORK_ERROR,
        INTERNAL_ERROR
    }

    int width;
    int height;
    boolean failed = false;
    String uid;
    String className;
    String param;
    String resultCB;
    Class<?> c;
    AdView owner;
    MediatedAdView mAV;
    AdRequester requester;

    protected boolean errorCBMade = false;
    protected boolean successCBMade = false;

    protected MediatedAdViewController() {

    }

    protected MediatedAdViewController(AdView owner, AdResponse response) throws Exception {
        //TODO: owner - second part is for testing when owner is null
        requester = owner != null ? owner.mAdFetcher : response.requester;
        width = response.getWidth();
        height = response.getHeight();
        uid = response.getMediatedUID();
        className = response.getMediatedViewClassName();
        param = response.getParameter();
        resultCB = response.getMediatedResultCB();
        this.owner = owner;

        Clog.d(Clog.mediationLogTag, Clog.getString(R.string.instantiating_class, className));

        try {
            c = Class.forName(className);

        } catch (ClassNotFoundException e) {
            Clog.e(Clog.mediationLogTag, Clog.getString(R.string.class_not_found_exception));
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
            throw e;
        }

        try {
            Object o = c.newInstance();
            mAV = (MediatedAdView) o;
        } catch (InstantiationException e) {
            Clog.e(Clog.mediationLogTag, Clog.getString(R.string.instantiation_exception));
            failed = true;
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
            throw e;
        } catch (IllegalAccessException e) {
            Clog.e(Clog.mediationLogTag, Clog.getString(R.string.illegal_access_exception));
            failed = true;
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
            throw e;
        } catch (ClassCastException e) {
            Clog.e(Clog.mediationLogTag, "Class cast exception", e);
            failed = true;
            onAdFailed(RESULT.MEDIATED_SDK_UNAVAILABLE);
            throw e;
        }
    }

    //TODO: owner dependency
    public void onAdLoaded() {
        if ((owner != null) && owner.getAdListener() != null) {
            owner.getAdListener().onAdLoaded(owner);
        }
        if (!successCBMade) {
            successCBMade = true;
            fireResultCB(RESULT.SUCCESS);
        }
    }

    public void onAdFailed(MediatedAdViewController.RESULT reason) {
        // callback will be called by AdView
        this.failed = true;

        if (!errorCBMade) {
            fireResultCB(reason);
            errorCBMade = true;
        }

    }

    public void onAdExpanded() {
        if (owner.getAdListener() != null) {
            owner.getAdListener().onAdExpanded(owner);
        }
    }

    public void onAdCollapsed() {
        if (owner.getAdListener() != null) {
            owner.getAdListener().onAdCollapsed(owner);
        }
    }

    public void onAdClicked() {
        if (owner.getAdListener() != null) {
            owner.getAdListener().onAdClicked(owner);
        }
    }

    public boolean failed() {
        return failed;
    }

    private void fireResultCB(final MediatedAdViewController.RESULT result) {

        //fire call to result cb url
        HTTPGet<Void, Void, HTTPResponse> cb = new HTTPGet<Void, Void, HTTPResponse>() {
            @Override
            protected void onPostExecute(HTTPResponse response) {
                if (requester == null) {
                    Clog.e(Clog.httpRespLogTag, Clog.getString(R.string.fire_cb_requester_null));
                    return;
                } else if (response == null) {
                    Clog.e(Clog.httpRespLogTag, Clog.getString(R.string.fire_cb_response_null));
                    return;
                }

                requester.dispatchResponse(new AdResponse(requester, response.getResponseBody(), response.getHeaders()));
            }

            @Override
            protected String getUrl() {
                return resultCB + "&reason=" + result.ordinal();
            }
        };

        // Spawn GET call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            cb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            cb.execute();
        }
    }
}
