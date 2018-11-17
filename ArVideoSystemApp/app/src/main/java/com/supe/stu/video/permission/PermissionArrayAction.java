package com.supe.stu.video.permission;

/**
 * 同时请求多个权限后执行的回调操作
 *
 * @author chenming03
 */
public interface PermissionArrayAction {

    public void onResult(String[] permissions, boolean[] grantResults);

}