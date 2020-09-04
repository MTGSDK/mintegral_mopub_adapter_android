package com.mintegral.adapter.interstitialvideoreward.interstitialvideorewardadapter;

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
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGRewardVideoHandler;

import com.mintegral.msdk.out.RewardVideoListener;


import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;


import java.util.HashMap;
import java.util.Map;


/**
 * Created by songjunjun on 17/8/31.
 */

public class MintegralInterstitialVideoAdapter extends CustomEventInterstitial implements RewardVideoListener {
    MTGRewardVideoHandler mInterstitialHandler;
    CustomEventInterstitialListener mCustomEventInterstitialListener;
    private String appid = "";
    private String appkey = "";
    private String unitId = "";
    private String mRewardId = "";
    private String mUserId = "your user id";
    private String placementId = "";


    @Override
    public void onLoadSuccess(String s, String s1) {

    }

    @Override
    protected void loadInterstitial(Context mContext, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        mCustomEventInterstitialListener = customEventInterstitialListener;
        try {
            appid = serverExtras.get("appId");
            unitId = serverExtras.get("unitId");
            appkey = serverExtras.get("appKey");
            mRewardId = serverExtras.get("rewardId");
            placementId = serverExtras.get("placementId");
            AdapterCommonUtil.addChannel();
            //获取当前的用户id  get User id
            if (localExtras != null) {
                Object mCustomer = localExtras.get("Rewarded-Video-Customer-Id");
                if (mCustomer instanceof String) {
                    mUserId = mCustomer.toString();
                }
            }
        } catch (Throwable e1) {
            e1.printStackTrace();
        }

        if (!TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey) && !TextUtils.isEmpty(unitId)) {
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
        if (mContext instanceof Activity) {
            mInterstitialHandler = new MTGRewardVideoHandler((Activity) mContext, placementId, unitId);
            mInterstitialHandler.setRewardVideoListener(this);

            mInterstitialHandler.load();
        }

    }

    @Override
    protected void showInterstitial() {
        if (mInterstitialHandler != null && mInterstitialHandler.isReady()) {
            mInterstitialHandler.show(mRewardId, mUserId);
        } else if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
        }
    }

    @Override
    protected void onInvalidate() {
    }

    @Override
    public void onVideoAdClicked(String s, String s1) {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }
        Log.e("Mintegral", "onInterstitialAdClick");
    }


    @Override
    public void onAdClose(boolean b, String s, float v) {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialDismissed();
        }
        Log.e("Mintegral", "onInterstitialClosed");
    }


    @Override
    public void onVideoLoadFail(String s) {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
        }
        Log.e("Mintegral", "onInterstitialLoadFail");
    }


    @Override
    public void onVideoLoadSuccess(String s, String s1) {
        Log.e("Mintegral", "onVideoLoadSuccess");
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialLoaded();
        }
    }


    @Override
    public void onShowFail(String s) {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
        }
        Log.e("Mintegral", "onInterstitialShowFail");
    }


    @Override
    public void onAdShow() {
        if (mCustomEventInterstitialListener != null) {
            mCustomEventInterstitialListener.onInterstitialShown();
        }
        Log.e("Mintegral", "onInterstitialShowSuccess");
    }

    @Override
    public void onVideoComplete(String s, String s1) {

    }

    @Override
    public void onEndcardShow(String s, String s1) {

    }
}
