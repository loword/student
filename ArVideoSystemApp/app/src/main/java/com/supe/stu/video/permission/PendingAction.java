package com.supe.stu.video.permission;

import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * 请求权限的Action
 *
 * @author chenming03
 */
public class PendingAction {

    public final String[] permissions;
    public final boolean[] result;
    public final String[] permsToRequest;
    public final PermissionArrayAction action;

    public PendingAction(String[] permissions, boolean[] result, String[] permsToRequest,
                         PermissionArrayAction action) {
        this.permissions = permissions;
        this.permsToRequest = permsToRequest;
        this.result = result;
        this.action = action;
    }

    public void onRequestPermissionsResult(String[] permsToRequest, int[] grantResults) {
        for (int i = 0; i < permsToRequest.length; i++) {
            boolean granted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
            int index = getIndex(permsToRequest[i]);
            if (index >= 0) {
                result[index] = granted;
            }
        }
        if (action != null) {
            action.onResult(permissions, result);
        }
    }

    private int getIndex(String permission) {
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(permissions[i], permission)) {
                return i;
            }
        }
        return -1;
    }

}
