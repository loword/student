package com.supe.stu.video.ui;

/**
 * Created by hanyong on 2018/7/19.
 */

import android.view.View;

import com.supe.stu.video.FaceUI;
import com.supe.stu.video.ui.rotateview.RotateImageView;


/**
 * 管理 摄像头、flash按钮显示隐藏旋转、
 */
public class TitleBarUIMgr {

    /**
     * 控制icon显示, 是否录制或拍照状态
     */
    private boolean mIsRecordState = false;

    /**
     * 是否使用后置摄像头
     */
    private boolean mIsUseBackCamera = true;

    FaceUI mFaceUI;

    // 返回按钮
    private RotateImageView mIconBack;
    // 切换摄像头按钮
    private RotateImageView mIconCamera;
    // 闪光灯按钮
    private RotateImageView mIconFlash;

    public TitleBarUIMgr(FaceUI faceUi) {
        mFaceUI = faceUi;
        mIconBack = mFaceUI.getIconBack();
        mIconCamera = mFaceUI.getIconCamera();
        mIconFlash = mFaceUI.getIconFlash();
        mIsUseBackCamera = mFaceUI.isBackCamera();
        mFaceUI.setARSwitchCameraCallback(new FaceUI.ARSwitchCameraCallback() {
            @Override
            public void onCameraSwitch(boolean result, boolean rear) {
                mIsUseBackCamera = rear;
                updateFlashIcon();
                updateCameraIcon();
                updateBackIcon();
            }
        });
    }

    /**
     * 设置录制状态
     *
     * @param showRecordState
     */
    public void setShowRecordState(boolean showRecordState) {
        mIsRecordState = showRecordState;
        updateFlashIcon();
        updateCameraIcon();
        updateBackIcon();
    }


    /**
     * 检测是否支持前摄像头预览
     */
    private boolean isFrontCameraPreviewSupported() {
        return mFaceUI.getARActivity().getARCameraManager().isFrontCameraPreviewSupported();
    }

    private void updateFlashIcon() {
        if (mIsUseBackCamera && !mIsRecordState) {
            mIconFlash.setVisibility(View.VISIBLE);
        } else {
            mIconFlash.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCameraIcon() {
        if (!mIsRecordState) {
            mIconCamera.setVisibility(View.VISIBLE);
        } else {
            mIconCamera.setVisibility(View.INVISIBLE);
        }
    }

    private void updateBackIcon() {
        if (!mIsRecordState) {
            mIconBack.setVisibility(View.VISIBLE);
        } else {
            mIconBack.setVisibility(View.INVISIBLE);
        }
    }

    public void release() {
        mFaceUI.setARSwitchCameraCallback(null);
    }

}
