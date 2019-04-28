package com.meishe.sdkdemo;

import android.app.Application;
import android.content.Context;

import com.meicam.effect.sdk.NvsEffectSdkContext;
import com.meicam.sdk.NvsStreamingContext;
import com.meishe.sdkdemo.utils.Logger;
import com.meishe.sdkdemo.utils.asset.NvAssetManager;
import com.meishe.sdkdemo.utils.authpack;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by ${gexinyu} on 2018/5/24.
 */

public class MSApplication extends Application {
    private static Context mContext;
    private static boolean mCanUseARFace = false;
    public static Context getmContext() {
        return mContext;
    }
    public static boolean isCanUseARFace(){
        return mCanUseARFace;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e("MSApplication","onCreate");
        mContext = getApplicationContext();
        //初始化
        String licensePath = "assets:/meishesdk.lic";
        NvsStreamingContext.init(getApplicationContext(), licensePath, NvsStreamingContext.STREAMING_CONTEXT_FLAG_SUPPORT_4K_EDIT);
        NvAssetManager.init(getApplicationContext());
        // 探测人脸特效是否可用
        try {
            Class.forName("com.meicam.sdk.NvsFaceEffectV1Detector");
            mCanUseARFace = true;
        } catch (ClassNotFoundException e) {
            mCanUseARFace = false;
            e.printStackTrace();
        }
        // 初始化AR Effect，全局只需一次
        if (mCanUseARFace) {
            com.meicam.sdk.NvsFaceEffectV1.setup("assets:/NvFaceData.asset", authpack.A());
            com.meicam.sdk.NvsFaceEffectV1.setMaxFaces(2);
        }

        //友盟初始化 Push推送业务的secret,没有为空
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
//        组件化的Log是否输出 默认关闭Log输出。  和集成测试是一个开关，release要关闭
//        UMConfigure.setLogEnabled(true);
        // isEnable: false-关闭错误统计功能；true-打开错误统计功能（默认打开）
//        public static void setCatchUncaughtExceptions(boolean isEnable)
        //场景类型设置
        MobclickAgent.setScenarioType(mContext, MobclickAgent.EScenarioType.E_UM_NORMAL);
    }
}
