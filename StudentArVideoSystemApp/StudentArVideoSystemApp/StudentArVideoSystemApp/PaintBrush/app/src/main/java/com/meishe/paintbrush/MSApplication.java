package com.meishe.paintbrush;

import android.app.Application;
import android.content.Context;


/**
 * Created by ms on 2018/10/12.
 */

public class MSApplication extends Application {
    private static Context mContext;

    public static Context getmContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
