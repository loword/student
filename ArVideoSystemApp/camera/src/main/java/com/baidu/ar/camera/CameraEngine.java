/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.ar.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huxiaowen on 2017/3/22.
 */

class CameraEngine {
    private static final String TAG = CameraEngine.class.getSimpleName();

    private static final int RETRY_OPEN_CAMERA_MAX = 3;
    private static final int RETRY_OPEN_CAMERA_DELAY_MS = 50;

    private Camera mCamera = null;

    private static volatile CameraEngine sInstance;

    public static CameraEngine getInstance() {
        if (sInstance == null) {
            synchronized (CameraEngine.class) {
                if (sInstance == null) {
                    sInstance = new CameraEngine();
                }
            }
        }
        return sInstance;
    }

    public static void releaseInstance() {
        sInstance = null;
    }

    private CameraEngine() {
    }

    public boolean openCamera(CameraParams cameraParams) {
        for (int i = 0; i < RETRY_OPEN_CAMERA_MAX; i++) {
            try {
                mCamera = Camera.open(cameraParams.getCameraId());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (mCamera != null) {
                    mCamera.release();
                }
                Thread.sleep(RETRY_OPEN_CAMERA_DELAY_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean setParameters(CameraParams cameraParams) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            if (cameraParams.isAutoCorrectParams()) {
                CameraHelper.correctCameraParams(cameraParams, parameters);
            }

            parameters.setPreviewSize(cameraParams.getPreviewWidth(), cameraParams.getPreviewHeight());
            parameters.setPreviewFrameRate(cameraParams.getFrameRate());
            parameters.setPictureSize(cameraParams.getPictureWidth(), cameraParams.getPictureHeight());
            parameters.setExposureCompensation(cameraParams.getExposureCompensation());
            if (cameraParams.isAutoFocus()) {
                CameraHelper.setAutoFocus(parameters);
            }
            mCamera.setDisplayOrientation(cameraParams.getRotateDegree());

            mCamera.setParameters(parameters);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setPreviewTexture(SurfaceTexture surfaceTexture) {
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setPreviewHolder(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setPreviewCallback(Camera.PreviewCallback previewCallback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(previewCallback);
            return true;
        }
        return false;
    }

    public boolean setPreviewCallbackWithBuffer(Camera.PreviewCallback previewCallback) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int previewBufferSize =
                    ((previewSize.width * previewSize.height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8;
            // 添加3个buffer，保障buffer足够使用，且不影响效率
            for (int i = 0; i < 3; i++) {
                mCamera.addCallbackBuffer(new byte[previewBufferSize]);
            }
            return true;
        }
        return false;
    }

    public boolean startPreview() {
        Log.d(TAG, "startPreview !!!");
        try {
            mCamera.startPreview();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean stopPreview() {
        Log.d(TAG, "stopPreview");
        try {
            mCamera.stopPreview();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void focusOnTouch(int x, int y, int viewWidth, int viewHeight) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumFocusAreas() <= 0) {
            mCamera.autoFocus(null);
            return;
        }
        mCamera.cancelAutoFocus();
        List<Camera.Area> areas = new ArrayList<>();
        List<Camera.Area> areasMetrix = new ArrayList<Camera.Area>();
        Rect focusRect = calculateTapArea(x, y, 1.0f, viewWidth, viewHeight);
        Rect metrixRect = calculateTapArea(x, y, 1.5f, viewWidth, viewHeight);
        areas.add(new Camera.Area(focusRect, 1000));
        areasMetrix.add(new Camera.Area(metrixRect, 1000));
        parameters.setMeteringAreas(areasMetrix);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFocusAreas(areas);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.autoFocus(null);
    }

    private Rect calculateTapArea(float x, float y, float coefficient, int viewWidth, int viewHeight) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY = 0;
        int centerX = 0;
        centerY = (int) ((x / viewWidth) * 2000 - 1000);
        centerX = (int) ((y / viewHeight) * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right),
                Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public boolean openFlash() {
        try {
            if (CameraHelper.isBackCameraCurrent()) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (!Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean closeFlash() {
        try {
            if (CameraHelper.isBackCameraCurrent()) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (!Camera.Parameters.FLASH_MODE_OFF.equals(parameters.getFlashMode())) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void release() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.release();
            mCamera = null;
        }
        releaseInstance();
    }

}