package com.mintegral.adapter.interstitial.interstitialadapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.mintegral.adapter.common.AdapterCommonUtil;
import com.mintegral.adapter.common.AdapterTools;
import com.mintegral.adapter.common.MIntegralSDKManager;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.InterstitialListener;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGInterstitialHandler;

import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songjunjun on 17/8/31.
 */

public class MintegralInterstitialAdapter extends CustomEventInterstitial implements InterstitialListener {
    MTGInterstitialHandler mInterstitialHandler;
    CustomEventInterstitialListener mCustomEventInterstitialListener;
    private String appid = "";
    private String appkey = "";
    private String unitId = "";

    @Override
    protected void loadInterstitial(Context mContext, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        mCustomEventInterstitialListener = customEventInterstitialListener;

        try {
            appid = serverExtras.get("appId");
            unitId = serverExtras.get("unitId");
            appkey = serverExtras.get("appKey");
            AdapterCommonUtil.addChannel();
        } catch (Throwable e1) {
            e1.printStackTrace();
        }

        if (!TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey) && !TextUtils.isEmpty(unitId)) {
            if (!AdapterTools.canCollectPersonalInformation()) {
                MIntegralSDKManager.getInstance().getMIntegralSDK().setUserPrivateInfoType(mContext, MIntegralConstans.AUTHORITY_ALL_INFO, MIntegralConstans.IS_SWITCH_OFF);
            } else {
                MIntegralSDKManager.getInstance().getMIntegralSDK().setUserPrivateInfoType(mContext, MIntegralConstans.AUTHORITY_ALL_INFO, MIntegralConstans.IS_SWITCH_ON);
            }

            if (mContext instanceof Activity) {
                final Context context = ((Activity) mContext).getApplication();
                MIntegralSDKManager.getInstance().initialize(context, appkey, appid, false);
            } else if (mContext instanceof Application) {
                final Context context = mContext;
                MIntegralSDKManager.getInstance().initialize(context, appkey, appid, false);
            }
            AdapterCommonUtil.parseLocalExtras(localExtras, MIntegralSDKManager.getInstance().getMIntegralSDK());
        } else {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }
        //设置广告位ID 必填
        hashMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
        mInterstitialHandler = new MTGInterstitialHandler(mContext, hashMap);
        mInterstitialHandler.setInterstitialListener(this);
        mInterstitialHandler.preload();
    }


    @Override
    protected void showInterstitial() {
        if (mInterstitialHandler != null) {
            mInterstitialHandler.show();
        }
    }

    @Override
    protected void onInvalidate() {
        Log.e("Mintegral", "onInvalidate");
//        mCustomEventInterstitialListener.
    }

    @Override
    public void onInterstitialAdClick() {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialClicked();
            mCustomEventInterstitialListener.onLeaveApplication();
        }
        Log.e("Mintegral", "onInterstitialAdClick");
    }

    @Override
    public void onInterstitialClosed() {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialDismissed();
        }
        Log.e("Mintegral", "onInterstitialClosed");
    }

    @Override
    public void onInterstitialLoadFail(String s) {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
        }
        Log.e("Mintegral", "onInterstitialLoadFail");
    }

    @Override
    public void onInterstitialLoadSuccess() {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialLoaded();
        }
        Log.e("Mintegral", "onInterstitialLoadSuccess");
    }

    @Override
    public void onInterstitialShowFail(String s) {
        Log.e("Mintegral", "onInterstitialShowFail");
    }

    @Override
    public void onInterstitialShowSuccess() {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialShown();
        }
        Log.e("Mintegral", "onInterstitialShowSuccess");
    }


}
