package com.supe.stu.video.camera;

import android.graphics.SurfaceTexture;

/**
 * Created by hanyong on 2018/7/19.
 */

public interface ARCameraCallback {
    void onCameraStart(boolean result, SurfaceTexture surfaceTexture, int width, int height);

    void onStopCamera(boolean result);

    void onPreviewFrame(byte[] data, int width, int height);
}
