package com.supe.stu.video.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment中权限管理
 *
 * @author chenming03
 */
public class FaceARPermissions {

    private final Map<Integer, PendingAction> mPendingActionMap = new HashMap<>();

    private Activity mActivity;

    public FaceARPermissions(Activity activity) {
        mActivity = activity;
    }

    /**
     * 根据用户选择的结果，处理权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PendingAction pendingAction = mPendingActionMap.remove(requestCode);
        if (pendingAction != null) {
            pendingAction.onRequestPermissionsResult(permissions, grantResults);
        }
    }

    /**
     * 请求单个权限
     *
     * @param permission
     * @param action
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestForBaidu(String permission, PermissionAction action) {
        // 6.0以下版本直接同意使用权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Permissions.granted(action);
            return;
        }
        // 检测权限
        int permissionCheck = mActivity.checkSelfPermission(permission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // 执行获取权限后的操作
            Permissions.granted(action);
            return;
        }

        // 判断是否还能显示权限对话框
        boolean shouldShowRequest =
                Permissions.shouldShowRequestPermissionRationale(mActivity, permission);
        if (shouldShowRequest) {
            // 执行没有获取权限的操作
            requestPermission(permission, action);
        } else {
            // 已拒绝,并且不会再显示权限对话框,需要提示用户在Settings->App->权限中打开权限
            Permissions.denied(action);
        }
    }

    /**
     * 请求单个权限
     *
     * @param permission
     * @param action
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void request(String permission, PermissionAction action) {
        // 6.0以下版本直接同意使用权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Permissions.granted(action);
            return;
        }
        // 检测权限
        int permissionCheck = mActivity.checkSelfPermission(permission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // 执行获取权限后的操作
            Permissions.granted(action);
            return;
        }
        // 判断是否还能显示权限对话框
        boolean shouldShowRequest =
                Permissions.shouldShowRequestPermissionRationale(mActivity, permission);
        if (shouldShowRequest) {
            // 执行没有获取权限的操作
            requestPermission(permission, action);
        } else {
            // 已拒绝,并且不会再显示权限对话框,需要提示用户在Settings->App->权限中打开权限
            Permissions.denied(action);
        }
    }

    /**
     * 同时请求多个权限
     *
     * @param permissions
     * @param action
     */
    public void request(String[] permissions, PermissionArrayAction action) {
        // 6.0以下版本直接同意使用权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Permissions.granted(action, permissions);
            return;
        }

        boolean[] result = new boolean[permissions.length];
        List<String> permsToRequestList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            // 检测权限
            String permission = permissions[i];
            int permissionCheck = mActivity.checkSelfPermission(permission);
            boolean granted = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            if (!granted) {
                // 判断是否还能显示权限对话框
                boolean shouldShowRequest =
                        Permissions.shouldShowRequestPermissionRationale(mActivity, permission);
                if (shouldShowRequest) {
                    permsToRequestList.add(permission);
                }
            }
            result[i] = granted;
        }
        if (permsToRequestList.size() == 0) {
            if (action != null) {
                action.onResult(permissions, result);
            }
            return;
        }

        String[] permsToRequest = permsToRequestList.toArray(new String[permsToRequestList.size()]);
        requestPermission(permissions, permsToRequest, result, action);
    }

    /**
     * 请求权限
     *
     * @param permission
     * @param action
     */
    private void requestPermission(String permission, final PermissionAction action) {
        String[] permissions = new String[]{permission};
        boolean[] result = new boolean[1];
        result[0] = false;
        PermissionArrayAction arrayAction = new PermissionArrayAction() {
            @Override
            public void onResult(String[] permissions, boolean[] grantResults) {
                if (grantResults[0]) {
                    Permissions.granted(action);
                } else {
                    Permissions.denied(action);
                }
            }
        };
        requestPermission(permissions, permissions, result, arrayAction);
    }

    /**
     * 请求权限
     *
     * @param permissions
     * @param permsToRequest
     * @param result
     * @param action
     */
    private void requestPermission(String[] permissions, String[] permsToRequest,
                                   boolean[] result, PermissionArrayAction action) {
        int code = Permissions.genRequestCode();
        PendingAction
                pendingAction = new PendingAction(permissions, result, permsToRequest, action);
        mPendingActionMap.put(code, pendingAction);
        mActivity.requestPermissions(permsToRequest, code);
    }

    /**
     * 是否有Audio权限
     *
     * @return
     */
    public boolean hasAudioPermission() {
        return Permissions.hasPermission(mActivity, Manifest.permission.RECORD_AUDIO);
    }

}
