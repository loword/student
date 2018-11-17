package com.supe.stu.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.baidu.ar.ARController;
import com.baidu.ar.DuMixCallback;
import com.baidu.ar.DuMixSource;
import com.baidu.ar.DuMixTarget;
import com.baidu.ar.base.MsgField;
import com.baidu.ar.bean.ARConfig;
import com.baidu.ar.bean.ARResource;
import com.baidu.ar.camera.CameraParams;
import com.baidu.ar.constants.ARConfigKey;
import com.baidu.ar.rotate.Orientation;
import com.baidu.ar.rotate.OrientationManager;
import com.baidu.ar.statistic.StatisticConstants;
import com.baidu.ar.statistic.StatisticHelper;
import com.baidu.ar.util.ARLog;
import com.baidu.ar.util.Res;
import com.baidu.ar.util.SystemInfoUtil;
import com.baidu.ar.util.UiThreadUtil;
import com.supe.stu.video.camera.ARCameraCallback;
import com.supe.stu.video.camera.ARCameraManager;
import com.supe.stu.video.draw.ARRenderCallback;
import com.supe.stu.video.permission.FaceARPermissions;
import com.supe.stu.video.utils.DeviceInfoUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by hanyong on 2018/7/19.
 */

public class ARActivity extends Activity implements ARCameraCallback,
        SurfaceTexture.OnFrameAvailableListener, OrientationManager.OrientationListener {
    private ARController mARController;
    private ARDuMixCallback mARDuMixCallback;
    private FaceUI mFaceUI;

    private static boolean isStarted;

    private CameraParams mCameraParams;

    /**
     * AR相机管理
     */
    private ARCameraManager mARCameraManager;
    private FaceARPermissions mFaceARPermissions;

    /**
     * 屏幕方向管理
     * 人脸检测方向需要
     */
    private OrientationManager mOrientationManager = null;
    ARControllerOrientation mAROrientation;

    private DuMixSource mDuMixSource;
    private DuMixTarget mDuMixTarget;

    public static void startActivity(Context context) {
        if (!FaceARApplication.activityIsFool("ARActivity")) {
            Intent intent = new Intent(context, ARActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bdar_layout_arui);
        Res.addResource(this);
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(ARConfigKey.AR_KEY, 1);
            jsonObj.put(ARConfigKey.AR_TYPE, ARConfig.TYPE_FACE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ARConfig.initARConfig(jsonObj.toString());
        mARCameraManager = new ARCameraManager();
        mARCameraManager.setCameraCallback(this);
        setCameraParams();
        mARDuMixCallback = new ARDuMixCallback();

        boolean isFront = mCameraParams.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mFaceUI = new FaceUI(this, !isFront);

        mAROrientation = new ARControllerOrientation(ARActivity.this);
        mOrientationManager = new OrientationManager(getApplicationContext());
        boolean isLandscape = SystemInfoUtil.isScreenOrientationLandscape(ARActivity.this);
        mOrientationManager.setScreenOrientationLandscape(isLandscape);
        mFaceARPermissions = new FaceARPermissions(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mAROrientation.enable();
            mOrientationManager.enable();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mARController != null) {
            mARController.resume();
        }
        if (mFaceUI != null) {
            mFaceUI.onResume();
        }
        startCamera();
    }

    private void initArBlend() {
        mFaceUI.getARRenderer().setARRenderCallback(new ARRenderCallback() {
            @Override
            public void onCameraDrawerCreated(SurfaceTexture surfaceTexture, int width, int height) {
                mDuMixSource = new DuMixSource(surfaceTexture, width, height);
                mDuMixSource.setArKey(ARConfig.getARKey());
                mDuMixSource.setArType(ARConfig.getARType());
                mDuMixSource.setFrontCamera(!mFaceUI.isBackCamera());
            }

            @Override
            public void onARDrawerCreated(SurfaceTexture surfaceTexture, SurfaceTexture.OnFrameAvailableListener
                    arFrameListener, int width, int height) {

                if (SystemInfoUtil.isScreenOrientationLandscape(ARActivity.this)) {
                    mDuMixTarget = new DuMixTarget(surfaceTexture, arFrameListener, height, width, true);
                } else {
                    mDuMixTarget = new DuMixTarget(surfaceTexture, arFrameListener, width, height, true);
                }

                if (mDuMixSource != null && ARConfig.getARType() != ARConfig.TYPE_FACE) {
                    mDuMixSource.setCameraSource(null);
                }
                if (mARController != null) {
                    mARController.setup(mDuMixSource, mDuMixTarget, mARDuMixCallback);
                    mARController.resume();
                } else {
                    ARLog.e("onCreateView mARController is NULLLLLL!!!");
                }
            }

            @Override
            public void onARDrawerChanged(SurfaceTexture surfaceTexture, int width, int height) {
                if (mARController != null) {
                    if (SystemInfoUtil.isScreenOrientationLandscape(ARActivity.this)) {
                        mARController.reSetup(surfaceTexture, height, width);
                    } else {
                        mARController.reSetup(surfaceTexture, width, height);
                    }
                }
            }
        });

        mOrientationManager.addOrientationListener(this);
    }

    private void setCameraParams() {
        mCameraParams = new CameraParams();
        if (ARConfig.getARType() == ARConfig.TYPE_FACE) {
            mCameraParams.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        if (DeviceInfoUtils.isMIUI()) {
            mCameraParams.setExposureCompensation(2);
        }
        //        else if (DeviceInfoUtils.isOppo()
        //                || DeviceInfoUtils.isVivo()
        //                || DeviceInfoUtils.isHuaWei()) {
        //            mCameraParams.setExposureCompensation(-4);
        //        }
        mARCameraManager.setCameraParams(mCameraParams);
    }

    public ARCameraManager getARCameraManager() {
        return mARCameraManager;
    }

    public ARController getARController() {
        return mARController;
    }

    public FaceARPermissions getFaceARPermissions() {
        return mFaceARPermissions;
    }

    /**
     * 开始打开相机
     */
    private void startCamera() {
        SurfaceTexture surfaceTexture = mFaceUI.getARRenderer().getCameraTexture();
        mFaceUI.getARRenderer().setCameraFrameListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //                onCameraFirstFrame();
            }
        });
        StatisticHelper.getInstance().statisticInfo(StatisticConstants.OPEN_CAMERA);
        mARCameraManager.startCamera(surfaceTexture);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mOrientationManager) {
            mAROrientation.disable();
            mOrientationManager.disable();
        }
        if (mFaceUI != null) {
            mFaceUI.onPause();
        }
        mARCameraManager.stopCamera();
        if (mARController != null) {
            mARController.pause();
        }

        if (mFaceUI != null) {
            mFaceUI.activityFinishPre();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFaceUI != null) {
            if (mFaceUI.onBackPressed()) {
                super.onBackPressed();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mOrientationManager) {
            mAROrientation.disable();
            mOrientationManager.disable();
        }
        if (mFaceUI != null) {
            mFaceUI.release();
        }
        if (mARController != null) {
            mARController.release();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // mGLSurfaceView.requestRender();
    }

    @Override
    public void onCameraStart(boolean result, SurfaceTexture surfaceTexture, int width, int height) {
        if (result) {
            // 相机打开后加载插件
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) {
                        return;
                    }
                    if (mARController == null) {
                        mARController = new ARController(ARActivity.this);
                        initArBlend();
                    }
                }
            });
        }
    }

    @Override
    public void onStopCamera(boolean result) {

    }

    @Override
    public void onPreviewFrame(byte[] data, int width, int height) {
        if (mARController != null) {
            mARController.onCameraPreviewFrame(data, width, height);
        }
    }

    @Override
    public void onRotateOrientation(Orientation orientation) {
        if (mFaceUI != null) {
            mFaceUI.rotateOrientation(orientation);
        }
    }

    private static class ARControllerOrientation extends OrientationEventListener {
        private WeakReference<ARActivity> mUIControllerRef;

        public ARControllerOrientation(ARActivity arActivity) {
            super(arActivity);
            mUIControllerRef = new WeakReference<ARActivity>(arActivity);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (mUIControllerRef.get() != null && mUIControllerRef.get().mARController != null) {
                mUIControllerRef.get().mARController.orientationChange(orientation);
            }
        }
    }

    /**
     * 实现DuMixCallback的回调接口类
     */
    private class ARDuMixCallback implements DuMixCallback {
        @Override
        public void onStateChange(int state, Object data) {
            int a = 0;
            switch (state) {
                case MsgField.MSG_AUTH_FAIL:
                    String toastStr = (String) data;
                    mFaceUI.onAuth(false);
                    Toast.makeText(ARActivity.this, toastStr, Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        }

        @Override
        public void onLuaMessage(HashMap<String, Object> luaMsg) {

        }

        @Override
        public void onStateError(int error, String msg) {

        }

        @Override
        public void onSetup(boolean result) {
            if (result && mFaceUI != null) {
                mFaceUI.onReady();
            }
        }

        @Override
        public void onCaseChange(boolean result) {

        }

        @Override
        public void onCaseCreated(ARResource arResource) {
            if (mFaceUI != null) {
                mFaceUI.loadFaceAssets(arResource);
            }
        }

        @Override
        public void onPause(boolean result) {

        }

        @Override
        public void onResume(boolean result) {

        }

        @Override
        public void onReset(boolean result) {

        }

        @Override
        public void onRelease(boolean result) {

        }
    }

    @Override
    public void finish() {
        super.finish();
        isStarted = false;
        FaceARApplication.clearActivityList();
    }
}
