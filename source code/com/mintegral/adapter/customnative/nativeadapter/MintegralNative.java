package com.mintegral.adapter.customnative.nativeadapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.mintegral.adapter.common.AdapterCommonUtil;
import com.mintegral.adapter.common.AdapterTools;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.Frame;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MtgNativeHandler;
import com.mintegral.msdk.out.NativeListener;


import com.mopub.nativeads.CustomEventNative;
import com.mopub.nativeads.ImpressionTracker;
import com.mopub.nativeads.NativeClickHandler;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeImageHelper;
import com.mopub.nativeads.StaticNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mopub.nativeads.NativeImageHelper.preCacheImages;


/**
 * Created by songjunjun on 16/11/15.
 */

public class MintegralNative extends CustomEventNative {

    private static final String TAG="MintegralNative";

    @Override
    protected void loadNativeAd(@NonNull Context context, @NonNull CustomEventNativeListener customEventNativeListener, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {


        final String unit_id;


        if (extrasAreValid(serverExtras, context,localExtras)) {
            unit_id = serverExtras.get("unitId");
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        MintegralStaticNativeAd mintegralStaticNativeAd = new MintegralStaticNativeAd(unit_id,
                context,
                new ImpressionTracker(context),
                new NativeClickHandler(context),
                customEventNativeListener);
        mintegralStaticNativeAd.loadAd();
    }



    private boolean extrasAreValid(final Map<String, String> serverExtras, Context mContext,Map<String, Object> localExtras) {
        final String placementId = serverExtras.get("appId");
        final String appKey = serverExtras.get("appKey");

        AdapterCommonUtil.addChannel();
        if (placementId != null && placementId.length() > 0 && appKey != null && appKey.length() > 0) {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            if(!AdapterTools.canCollectPersonalInformation()){
                sdk.setUserPrivateInfoType(mContext, MIntegralConstans.AUTHORITY_ALL_INFO,MIntegralConstans.IS_SWITCH_OFF);
            }else{
                sdk.setUserPrivateInfoType(mContext,MIntegralConstans.AUTHORITY_ALL_INFO,MIntegralConstans.IS_SWITCH_ON);
            }

            Map<String, String> map = sdk.getMTGConfigurationMap(placementId,
                    appKey);
            if (mContext instanceof Activity) {
                sdk.init(map, ((Activity) mContext).getApplication());
            } else if (mContext instanceof Application) {
                sdk.init(map, mContext);
            }
            AdapterCommonUtil.parseLocalExtras(localExtras,sdk);
            return true;
        }

        return false;
    }


    public static class MintegralStaticNativeAd extends StaticNativeAd implements NativeListener.NativeAdListener, NativeListener.NativeTrackingListener {

        MtgNativeHandler nativeHandle;
        NativeClickHandler mNativeClickHandler;
        ImpressionTracker mImpressionTracker;
        CustomEventNativeListener customEventNativeListener;
        Context context;

       public Campaign campaign;

        public MintegralStaticNativeAd(String unit_id, final Context mContext, final ImpressionTracker impressionTracker,
                                      @NonNull final NativeClickHandler nativeClickHandler, final CustomEventNativeListener customEventNativeListener) {


            this.customEventNativeListener = customEventNativeListener;
            mImpressionTracker = impressionTracker;
            mNativeClickHandler = nativeClickHandler;
            context = mContext;


            Map<String, Object> properties = MtgNativeHandler.getNativeProperties(unit_id);

            properties.put(MIntegralConstans.PROPERTIES_AD_NUM, 1);
            properties.put(MIntegralConstans.NATIVE_VIDEO_WIDTH, 720);
            properties.put(MIntegralConstans.NATIVE_VIDEO_HEIGHT, 480);
            properties.put(MIntegralConstans.NATIVE_VIDEO_SUPPORT,true);
            nativeHandle = new MtgNativeHandler(properties, mContext);

        }


        @Override
        public void onStartRedirection(Campaign campaign, String url) {

        }

        @Override
        public void onRedirectionFailed(Campaign campaign, String url) {

        }

        @Override
        public void onFinishRedirection(Campaign campaign, String url) {

        }

        @Override
        public void onDownloadStart(Campaign campaign) {

        }

        @Override
        public void onDownloadFinish(Campaign campaign) {

        }

        @Override
        public void onDownloadProgress(int progress) {

        }

        @Override
        public boolean onInterceptDefaultLoadingDialog() {

            return false;
        }

        @Override
        public void onShowLoading(Campaign campaign) {

        }

        @Override
        public void onDismissLoading(Campaign campaign) {

        }

        @Override
        public void onAdLoaded(List<Campaign> campaigns, int template) {

            final List<String> imageUrls = new ArrayList<String>();
            if (campaigns != null && campaigns.size() > 0) {
                for (Campaign mCampaign : campaigns
                        ) {//将返回的所有广告给赋值
                    setMainImageUrl(mCampaign.getImageUrl());
                    if (!TextUtils.isEmpty(mCampaign.getImageUrl())) {
                        imageUrls.add(mCampaign.getImageUrl());
                    }

                    setIconImageUrl(mCampaign.getIconUrl());
                    if (!TextUtils.isEmpty(mCampaign.getIconUrl())) {
                        imageUrls.add(mCampaign.getIconUrl());
                    }

                    setStarRating(mCampaign.getRating());
                    setCallToAction(mCampaign.getAdCall());
                    setTitle(mCampaign.getAppName());
                    setText(mCampaign.getAppDesc());

                    campaign = mCampaign;
                }
            } else {
                customEventNativeListener.onNativeAdFailed(NativeErrorCode.EMPTY_AD_RESPONSE);
            }

            preCacheImages(context, imageUrls, new NativeImageHelper.ImageListener() {
                @Override
                public void onImagesCached() {
                    customEventNativeListener.onNativeAdLoaded(MintegralStaticNativeAd.this);
                }

                @Override
                public void onImagesFailedToCache(NativeErrorCode errorCode) {
                    customEventNativeListener.onNativeAdFailed(errorCode);
                }
            });

        }

        @Override
        public void onAdLoadError(String msg) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);

        }

        @Override
        public void onAdClick(Campaign campaign) {
            Log.e("", "====onAdClick");//点击后回调
            notifyAdClicked();
        }


        @Override
        public void onAdFramesLoaded(final List<Frame> list) {

        }

        @Override
        public void onLoggingImpression(int adsourceType) {
            Log.e(TAG, "onLoggingImpression adsourceType:"+adsourceType);
            notifyAdImpressed();
        }


        @Override
        public void recordImpression(@NonNull View view) {
            super.recordImpression(view);


        }


        @Override
        public void prepare(@NonNull View view) {
            Log.e(TAG, "registerView");
            nativeHandle.registerView(view,campaign);
        }

        @Override
        public void clear(@NonNull View view) {

            mImpressionTracker.removeView(view);
            // mNativeClickHandler.clearOnClickListener(view);
        }

        @Override
        public void destroy() {
            super.destroy();
            nativeHandle.release();
        }

        @Override
        public void handleClick(@NonNull View view) {
//            super.handleClick(view);


        }

//        @Override
//        public void setNativeEventListener(@Nullable NativeEventListener nativeEventListener) {
//
//        }

        public void loadAd() {
            if (nativeHandle != null) {
                nativeHandle.setAdListener(this);
                nativeHandle.setTrackingListener(this);
                nativeHandle.load();
            } else {
                customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
            }

        }
    }
}
