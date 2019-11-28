package com.mintegral.adapter.reward.rewardadapter;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.mintegral.adapter.common.AdapterCommonUtil;
import com.mintegral.adapter.common.AdapterTools;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;


import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;
import com.mopub.mobileads.CustomEventRewardedVideo;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * Created by songjunjun on 17/3/22.
 */

public class MintegralRewardVideo extends CustomEventRewardedVideo implements RewardVideoListener {


    private JSONObject serverParams;
    boolean isInitialized = false;
    private String appid = "";
    private String appkey = "";
    private String unitId = "";
    private String mRewardId = "";
    private String mUserId = "your user id";
    private String packageName = "";//If ApplicationId and PackageName are not the same,the developers need give us the packagename


    private MTGRewardVideoHandler mMvRewardVideoHandler;
    private final static int LOAD_CANCEL_TIME = 20*1000;
    private final static int TIME_OUT_CODE = 0X001;
    private boolean hasRetrue = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TIME_OUT_CODE:
                    if(mMvRewardVideoHandler != null && !hasRetrue ){
                        hasRetrue = true;
                        if(mMvRewardVideoHandler.isReady()){
                            MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(MintegralRewardVideo.class, unitId);
                        }else{
                            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(MintegralRewardVideo.class, unitId, MoPubErrorCode.NETWORK_TIMEOUT);
                        }
                    }
                    break;
            }
        }
    };


    @Override
    public void onLoadSuccess(String s) {

    }

    @Override
    public void onAdClose(boolean b, String s, float v) {
        MoPubRewardedVideoManager.onRewardedVideoClosed(MintegralRewardVideo.class, unitId);
        if (b) {
            MoPubRewardedVideoManager.onRewardedVideoCompleted(MintegralRewardVideo.class, null, MoPubReward.success(s, (int) v));
        }

    }

    @Override
    public void onVideoLoadFail(String s) {
        Log.e("mvtest","====owner  onVideoLoadFail"+s);
        if(!hasRetrue){
            hasRetrue = true;
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(MintegralRewardVideo.class, unitId, MoPubErrorCode.UNSPECIFIED);

        }
       }

    @Override
    public void onVideoLoadSuccess(String s) {
        Log.e("mvtest","====owner  onVideoLoadSuccess"+s);
        if(!hasRetrue){
            hasRetrue = true;
            MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(MintegralRewardVideo.class, unitId);
        }

    }

    @Override
    public void onAdShow() {

        MoPubRewardedVideoManager.onRewardedVideoStarted(MintegralRewardVideo.class, unitId);
    }

    @Override
    public void onShowFail(String s) {

        MoPubRewardedVideoManager.onRewardedVideoPlaybackError(MintegralRewardVideo.class, unitId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
    }


    @Override
    public void onVideoAdClicked(String s) {
        MoPubRewardedVideoManager.onRewardedVideoClicked(MintegralRewardVideo.class, unitId);
    }

    @Override
    public void onEndcardShow(String s) {
    }


    @Override
    public void onVideoComplete(String s) {
//        MoPubRewardedVideoManager.onRewardedVideoCompleted(MintegralRewardVideo.class,unitId);
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {


        //获取当前的用户id  get User id
        if (localExtras != null) {
            Object mCustomer = localExtras.get("Rewarded-Video-Customer-Id");
            if (mCustomer instanceof String) {
                mUserId = mCustomer.toString();
            }
        }
        AdapterCommonUtil.addChannel();

        try {
            serverParams = new JSONObject(serverExtras);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (launcherActivity == null) {
            MoPubRewardedVideoManager
                    .onRewardedVideoLoadFailure(MintegralRewardVideo.class, getAdNetworkId(), MoPubErrorCode.UNSPECIFIED);
            return false;
        }

        checkApplicationIdAndUserId();//检查unity的是否传参数
        try {
            appid = serverParams.getString("appId");
            unitId = serverParams.getString("unitId");
            appkey = serverParams.getString("appKey");
            mRewardId = serverParams.getString("rewardId");
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (!isInitialized && !TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey)) {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            if(!AdapterTools.canCollectPersonalInformation()){
                sdk.setUserPrivateInfoType(launcherActivity.getApplication(), MIntegralConstans.AUTHORITY_ALL_INFO,MIntegralConstans.IS_SWITCH_OFF);
            }else{
                sdk.setUserPrivateInfoType(launcherActivity.getApplication(),MIntegralConstans.AUTHORITY_ALL_INFO,MIntegralConstans.IS_SWITCH_ON);
            }
            Map<String, String> map = sdk.getMTGConfigurationMap(appid, appkey);

            checkAndInitMediationSettings();
            if (!TextUtils.isEmpty(packageName)) {
                map.put(MIntegralConstans.PACKAGE_NAME_MANIFEST, packageName);
            }

            sdk.init(map, launcherActivity.getApplicationContext());
            isInitialized = true;
            AdapterCommonUtil.parseLocalExtras(localExtras,sdk);
        }
        return isInitialized;
    }



    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return unitId;
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {

        mMvRewardVideoHandler = new MTGRewardVideoHandler(activity, unitId);
        mMvRewardVideoHandler.setRewardVideoListener(this);
        AdapterCommonUtil.addChannel();

        Log.e("mvtest","====load");
        if(mHandler != null){
            mHandler.sendEmptyMessageDelayed(TIME_OUT_CODE,LOAD_CANCEL_TIME);
        }
        mMvRewardVideoHandler.load();
    }

    @Override
    protected void showVideo() {


        if (mMvRewardVideoHandler.isReady()) {
            mMvRewardVideoHandler.show(mRewardId, mUserId);
        } else {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(MintegralRewardVideo.class, unitId, MoPubErrorCode.VIDEO_CACHE_ERROR);
        }
    }


    @Override
    protected void onInvalidate() {

    }

    @Nullable
    @Override
    protected CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return null;
    }

    @Override
    protected boolean hasVideoAvailable() {
        return mMvRewardVideoHandler != null && mMvRewardVideoHandler.isReady();
    }


    private boolean checkAndInitMediationSettings() {
        MintegralMediationSettings globalMediationSettings = MoPubRewardedVideoManager.getGlobalMediationSettings(MintegralMediationSettings.class);
        if (globalMediationSettings != null) {

            if (!TextUtils.isEmpty(globalMediationSettings.getmPackageName())) {
                packageName = globalMediationSettings.getmPackageName();
            } else {
                return false;
            }

            return true;
        } else {
            return false;
        }

    }


    private void checkApplicationIdAndUserId() {
        try {


        } catch (Exception e) {
        }

    }

    public static final class MintegralMediationSettings implements MediationSettings {

        private final String mPackageName;

        public MintegralMediationSettings(String packageName) {
            this.mPackageName = packageName;
        }

        public String getmPackageName() {
            return mPackageName;
        }
    }
}
