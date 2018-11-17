/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.baidu.ar.camera.CameraHelper;
import com.baidu.ar.camera.CameraParams;
import com.baidu.ar.camera.easy.EasyCamera;
import com.baidu.ar.camera.easy.EasyCameraCallback;

/**
 * AR相机管理
 */
public class ARCameraManager implements EasyCameraCallback, Camera.PreviewCallback {
    private static final String TAG = ARCameraManager.class.getSimpleName();

    private CameraParams mCameraParams;
    private SurfaceTexture mSourceTexture;

    private ARCameraCallback mCameraCallback;
    private ARCameraOperationCallback mOperationcallback;

    public ARCameraManager() {
    }

    public void setCameraCallback(ARCameraCallback callback) {
        this.mCameraCallback = callback;
    }

    public void setOperationcallback(ARCameraOperationCallback callback) {
        this.mOperationcallback = callback;
    }

    public void setCameraParams(CameraParams cameraParams) {
        mCameraParams = cameraParams;
    }

    public void startCamera(SurfaceTexture sourceTexture) {
        mSourceTexture = sourceTexture;
        if (mCameraParams == null) {
            mCameraParams = new CameraParams();
        }
        EasyCamera.getInstance().startCamera(mCameraParams, mSourceTexture, this, this);
    }

    public void stopCamera() {
        EasyCamera.getInstance().stopCamera();

    }

    public void openFlash() {
        EasyCamera.getInstance().openFlash();
    }

    public void closeFlash() {
        EasyCamera.getInstance().closeFlash();
    }

    public void switchCamera() {
        EasyCamera.getInstance().switchCamera();
    }

    @Override
    public void onCameraStart(boolean result) {
        if (mCameraCallback != null && mCameraParams != null) {
            mCameraCallback.onCameraStart(result, mSourceTexture, mCameraParams.getPreviewWidth(),
                    mCameraParams.getPreviewHeight());
        }
    }

    @Override
    public void onCameraSwitch(boolean result) {
        if (mOperationcallback != null && mCameraParams != null) {
            mOperationcallback
                    .onCameraSwitch(result, mCameraParams.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    @Override
    public void onFlashOpen(boolean result) {
        if (mOperationcallback != null) {
            mOperationcallback.onFlashOpen(result);
        }
    }

    @Override
    public void onFlashClose(boolean result) {
        if (mOperationcallback != null) {
            mOperationcallback.onFlashClose(result);
        }
    }

    public void focusOnTouch(int x, int y, int viewWidth, int viewHeight) {
        EasyCamera.getInstance().focusOnTouch(x, y, viewWidth, viewHeight);
    }

    @Override
    public void onCameraStop(boolean result) {
        if (mCameraCallback != null) {
            mCameraCallback.onStopCamera(result);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraCallback != null) {
            mCameraCallback.onPreviewFrame(data, mCameraParams.getPreviewWidth(), mCameraParams.getPreviewHeight());
        }
    }

    /**
     * 判断是否支持前置摄像头
     *
     * @return
     */
    public boolean isFrontCameraPreviewSupported() {
        return CameraHelper.isCameraSupported(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
}
