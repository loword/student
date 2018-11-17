/**
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.ui.rotateview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.baidu.ar.rotate.Orientation;

/**
 * 支持旋转显示的图片控件
 *
 * @author chenming03
 */
public class RotateImageView extends ImageView implements IRotateView {

    /**
     * 当前视频旋转的角度
     */
    private int mAngle = 0;

    /**
     * 旋转动画
     */
    private Animation mAnimation;

    public RotateImageView(Context context) {
        this(context, null, 0);
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAngle == 0 || mAngle == 360) {
            super.onDraw(canvas);
        } else {
            canvas.save();
            canvas.rotate(mAngle, getWidth() * 0.5f, getHeight() * 0.5f);
            super.onDraw(canvas);
            canvas.restore();
        }
    }

    /**
     * 设置旋转角度
     *
     * @param angle
     */
    @Override
    public void setAngle(int angle) {
        mAngle = RotateViewUtils.calculateDegree(angle);
        invalidate();
    }

    @Override
    public int getAngle() {
        return mAngle;
    }

    /**
     * 不使用动画设置方向
     * 请求UI方向改变,不使用动画
     *
     * @param orien 三种情况---竖，横，反向横
     */
    @Override
    public void requestOrientation(Orientation orien) {
        RotateViewUtils.updateOrientation(this, orien);
    }

    /**
     * 请求UI方向改变
     *
     * @param orien      三种情况---竖，横，反向横
     * @param isAnimated 是否启用动画
     */
    @Override
    public void requestOrientation(Orientation orien, boolean isAnimated) {
        RotateViewUtils.updateOrientation(this, orien, isAnimated);
    }

    @Override
    public void startAnimation(Animation animation) {
        mAnimation = animation;
        super.startAnimation(animation);
    }

    @Override
    public void clearAnimation() {
        if (mAnimation != null) {
            mAnimation.cancel();
            mAnimation.setAnimationListener(null);
            mAnimation = null;
        }
        super.clearAnimation();
    }
}