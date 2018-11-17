package com.supe.stu.video.permission;

/**
 * 请求单个权限后执行的操作
 *
 * @author chenming03
 */
public interface PermissionAction {

    public void onGranted();

    public void onDenied();

}