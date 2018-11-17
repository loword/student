package com.supe.stu.video;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyong on 2018/7/19.
 */

public class FaceARApplication extends Application {

    /**
     * 上次按钮点击的时间
     */
    private static long mLastClickTime = 0;
    /**
     * 点击间隔
     */
    private static long CLICK_INTERVAL = 500;

    private static List<String> mActivityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public static boolean activityIsFool(String activityName) {
        if (mActivityList.size() > 0) {
            return true;
        }
        mActivityList.add(activityName);
        return false;
    }

    public static void clearActivityList() {
        mActivityList.clear();
    }

    /**
     * 是否可以点击,避免频繁点击问题
     */
    public static boolean canClick() {
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - mLastClickTime < CLICK_INTERVAL) {
            return false;
        } else {
            mLastClickTime = currentClickTime;
            return true;
        }
    }
}
