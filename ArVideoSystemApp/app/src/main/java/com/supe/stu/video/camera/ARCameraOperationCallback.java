/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.camera;

/**
 * 关于相机的相关结果回调（除了打开相机和切换相机）
 */
public interface ARCameraOperationCallback {

    /**
     * @param result 切换是否成功
     * @param rear   true表示后置摄像头 , false 前置摄像头
     */
    void onCameraSwitch(boolean result, boolean rear);

    void onFlashClose(boolean result);

    void onFlashOpen(boolean result);
}
