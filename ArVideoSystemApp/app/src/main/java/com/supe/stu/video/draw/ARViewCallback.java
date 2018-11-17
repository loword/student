/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.draw;

import android.graphics.SurfaceTexture;
import android.view.MotionEvent;

/**
 * 用于和插件版本传递参数
 */
public interface ARViewCallback {

    /**
     * arview初始化回调, 需要配置ar信息
     *
     * @param cameraSource
     * @param sourceWidth
     * @param sourceHeight
     * @param drawTarget
     * @param targetWidth
     * @param targetHeight
     * @param drawPreview
     */
    void setup(SurfaceTexture cameraSource, int sourceWidth,
               int sourceHeight, SurfaceTexture drawTarget, int targetWidth,
               int targetHeight, boolean drawPreview);

    /**
     * 相机数据帧回调, 此接口可移动到Camera来定义
     *
     * @param data
     * @param width
     * @param height
     */
    void onCameraPreviewFrame(byte[] data, int width, int height);

    /**
     * View touch事件处理
     *
     * @param event
     * @return
     */
    boolean onTouchEvent(MotionEvent event);
}
