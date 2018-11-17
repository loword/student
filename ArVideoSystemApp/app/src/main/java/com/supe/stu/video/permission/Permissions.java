package com.supe.stu.video.permission;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Android 6.0 权限管理
 *
 * @author chenming03
 */
public class Permissions {

    /**
     * 用于自动生成RequestCode
     */
    private static int sRequestCode = 153;

    /**
     * 自动生成requestCode
     *
     * @return
     */
    static int genRequestCode() {
        sRequestCode++;
        if (sRequestCode > 240) {
            sRequestCode = 150;
        }
        return sRequestCode;
    }

    /**
     * 执行获取权限后的操作
     *
     * @param action
     */
    static void granted(PermissionAction action) {
        if (action != null) {
            action.onGranted();
        }
    }

    /**
     * 执行获取权限后的操作
     *
     * @param action
     * @param permissions
     */
    static void granted(PermissionArrayAction action, String[] permissions) {
        if (action != null) {
            boolean[] result = new boolean[permissions.length];
            Arrays.fill(result, true);
            action.onResult(permissions, result);
        }
    }

    /**
     * 执行没有获取权限的操作
     *
     * @param action
     */
    static void denied(PermissionAction action) {
        if (action != null) {
            action.onDenied();
        }
    }

    /**
     * 判断是否再弹出对话框提示权限申请
     *
     * @param activity
     * @param permission
     * @return
     */
    static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        if (isFirstCheck(activity, permission)) {
            return true;
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * 是否第一次检测权限
     */
    private static boolean isFirstCheck(Context context, String permission) {
        SharedPreferences prefs = context.getSharedPreferences("permission_check", Context.MODE_PRIVATE);
        boolean first = prefs.getBoolean(permission, true);
        if (first) {
            prefs.edit().putBoolean(permission, false).commit();
        }
        return first;
    }

    /**
     * 判断是否拥有所有的权限
     *
     * @param grantResults
     * @return
     */
    public static boolean hasAllPermission(boolean[] grantResults) {
        for (boolean grantResult : grantResults) {
            if (!grantResult) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取需要未授予的权限
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static List<String> getDeniedPermissions(Activity activity, String[] permissions) {
        List<String> permissionsList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
        }
        return permissionsList;
    }

    /**
     * 判断是否有指定的权限
     *
     * @param activity
     * @param permission
     * @return
     */
    public static boolean hasPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
