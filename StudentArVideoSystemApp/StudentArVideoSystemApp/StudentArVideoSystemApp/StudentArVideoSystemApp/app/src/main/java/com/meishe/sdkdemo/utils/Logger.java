package com.meishe.sdkdemo.utils;

import android.util.Log;

import com.meishe.sdkdemo.BuildConfig;

/**
 * Created by CaoZhiChao on 2018/8/30 17:52
 */
public class Logger {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    public static void e(String TAG, Object msg) {
        if (DEBUG) {
            Log.e(TAG, String.valueOf(msg));
        }
    }
}
