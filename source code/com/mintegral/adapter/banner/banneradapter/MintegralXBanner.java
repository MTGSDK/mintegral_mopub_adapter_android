package com.mintegral.adapter.banner.banneradapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mintegral.adapter.common.AdapterCommonUtil;
import com.mintegral.adapter.common.AdapterTools;
import com.mintegral.adapter.common.MIntegralSDKManager;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.BannerAdListener;
import com.mintegral.msdk.out.BannerSize;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGBannerView;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Map;

import static com.mopub.common.DataKeys.AD_HEIGHT;
import static com.mopub.common.DataKeys.AD_WIDTH;

public class MintegralXBanner extends CustomEventBanner implements BannerAdListener {
    private final String TAG = "MintegralXBanner";
    private CustomEventBannerListener mBannerListener;
    private MTGBannerView mtgBannerView;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Log.e(TAG, "loadBanner: ");
        mBannerListener = customEventBannerListener;

        final String unitId;
        String placementId = "";

        if (extrasAreValid(serverExtras, context, localExtras)) {
            unitId = serverExtras.get("unitId");
            placementId = serverExtras.get("placementId");
        } else {
            customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (localExtras.containsKey(AD_WIDTH) && localExtras.containsKey(AD_HEIGHT)) {
            int adwidth = (int) localExtras.get(AD_WIDTH);
            int adheight = (int) localExtras.get(AD_HEIGHT);
            Log.e(TAG, "loadBanner: adwidth: " + adwidth + "- adheight: " + adheight);

            final int w = dip2px(context, adwidth);
            final int h = dip2px(context, adheight);

            mtgBannerView = new MTGBannerView(context);
            mtgBannerView.setVisibility(View.GONE);
            mtgBannerView.init(new BannerSize(BannerSize.DEV_SET_TYPE, adwidth, adheight), placementId, unitId);

            mtgBannerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    MoPubView.LayoutParams lp = (MoPubView.LayoutParams) mtgBannerView.getLayoutParams();
                    lp.width = w;
                    lp.height = h;
                    mtgBannerView.setLayoutParams(lp);
                }
            });

            mtgBannerView.setBannerAdListener(this);
            mtgBannerView.load();
        }
    }

    @Override
    protected void onInvalidate() {
        Log.e(TAG, "onInvalidate: ");
        if (mtgBannerView != null) {
            mtgBannerView.release();
        }
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras, Context mContext, Map<String, Object> localExtras) {
        final String appid = serverExtras.get("appId");
        final String appKey = serverExtras.get("appKey");
        final String unitId = serverExtras.get("unitId");

        AdapterCommonUtil.addChannel();
        if (!TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appKey) && !TextUtils.isEmpty(unitId)) {
            MIntegralSDK mIntegralSDK = MIntegralSDKManager.getInstance().getMIntegralSDK();
            if (!AdapterTools.canCollectPersonalInformation()) {
                mIntegralSDK.setUserPrivateInfoType(mContext, MIntegralConstans.AUTHORITY_ALL_INFO, MIntegralConstans.IS_SWITCH_OFF);
            } else {
                mIntegralSDK.setUserPrivateInfoType(mContext, MIntegralConstans.AUTHORITY_ALL_INFO, MIntegralConstans.IS_SWITCH_ON);
            }

            if (mContext instanceof Activity) {
                final Context context = ((Activity) mContext).getApplication();
                MIntegralSDKManager.getInstance().initialize(context, appKey, appid, false);
            } else if (mContext instanceof Application) {
                final Context context = mContext;
                MIntegralSDKManager.getInstance().initialize(context, appKey, appid, false);
            }
            AdapterCommonUtil.parseLocalExtras(localExtras, mIntegralSDK);
            return true;
        }

        return false;
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void onLoadFailed(String s) {
        if (mBannerListener != null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
        }
        Log.e(TAG, "onLoadFailed: " + s);
    }

    @Override
    public void onLoadSuccessed() {
        if (mBannerListener != null && mtgBannerView != null) {
            mBannerListener.onBannerLoaded(mtgBannerView);
            mtgBannerView.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "onLoadSuccessed: ");
    }

    @Override
    public void onLogImpression() {
        if (mBannerListener != null) {
            mBannerListener.onBannerImpression();
        }
        Log.d(TAG, "onLogImpression: ");
    }


    @Override
    public void onClick() {
        if (mBannerListener != null) {
            mBannerListener.onBannerClicked();
        }
        Log.d(TAG, "onClick: ");
    }

    @Override
    public void onLeaveApp() {
        if (mBannerListener != null) {
            mBannerListener.onLeaveApplication();
        }
        Log.d(TAG, "onLeaveApp: ");
    }

    @Override
    public void showFullScreen() {

        Log.d(TAG, "showFullScreen: ");
    }

    @Override
    public void closeFullScreen() {

        Log.d(TAG, "closeFullScreen: ");
    }

    @Override
    public void onCloseBanner() {

        Log.d(TAG, "onCloseBanner: ");

    }
}
