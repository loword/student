/**
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.ui.rotateview;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.baidu.ar.rotate.Orientation;

/**
 * 旋转View的常用工具类
 *
 * @author chenming03
 */
public class RotateViewUtils {

    /**
     * 0-顺转90
     */
    private static final int ANIM_TYPE_CLOCKWISE_90 = 0;

    /**
     * 1-逆转90
     */
    private static final int ANIM_TYPE_ANTICLOCKWISE_90 = 1;

    /**
     * 2-顺转180
     */
    private static final int ANIM_TYPE_CLOCKWISE_180 = 2;

    /**
     * request IRotateView Orientation
     *
     * @param v     IRotateView
     * @param orien Orientation
     */
    public static void requestOrientation(IRotateView v, Orientation orien) {
        if (null != v) {
            v.requestOrientation(orien);
        }
    }

    /**
     * request IRotateView Orientation
     *
     * @param v          IRotateView
     * @param orien      Orientation
     * @param isAnimated animation flag
     */
    public static void requestOrientation(IRotateView v, Orientation orien, boolean isAnimated) {
        if (null != v) {
            v.requestOrientation(orien, isAnimated);
        }
    }

    /**
     * 更新View方向
     *
     * @param view
     * @param orien
     */
    public static void updateOrientation(IRotateView view, Orientation orien) {
        switch (orien) {
            case PORTRAIT:
                // 设置竖屏显示
                view.setAngle(0);
                break;
            case LANDSCAPE:
                // 设置横屏显示
                view.setAngle(-90);
                break;
            case LANDSCAPE_REVERSE:
                // 设置反向横屏
                view.setAngle(90);
                break;
            default:
                break;
        }
    }

    /**
     * 请求UI方向改变
     *
     * @param orien      三种情况---竖，横，反向横
     * @param isAnimated 是否启用动画
     */
    static void updateOrientation(IRotateView view, Orientation orien, boolean isAnimated) {
        if (!view.isShown() || !isAnimated) {
            view.requestOrientation(orien);
            return;
        }
        view.clearAnimation();
        int angle = view.getAngle();
        switch (angle) {
            case 0:
            case 180:
            case 360: // 正常竖屏状态
                switch (orien) {
                    case PORTRAIT:
                        break;
                    case LANDSCAPE:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_ANTICLOCKWISE_90, orien);
                        break;
                    case LANDSCAPE_REVERSE:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_CLOCKWISE_90, orien);
                        break;
                    default:
                        break;
                }
                break;
            case 90:
                // 当前手机逆时针横屏(UI+90)
                switch (orien) {
                    case PORTRAIT:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_ANTICLOCKWISE_90, orien);
                        break;
                    case LANDSCAPE:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_CLOCKWISE_180, orien);
                        break;
                    case LANDSCAPE_REVERSE:
                        break;
                    default:
                        break;
                }
                break;
            case 270:
                // 已经处于逆时针90度
                switch (orien) {
                    case PORTRAIT:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_CLOCKWISE_90, orien);
                        break;
                    case LANDSCAPE:
                        break;
                    case LANDSCAPE_REVERSE:
                        startRotateAnimation(view, RotateViewUtils.ANIM_TYPE_CLOCKWISE_180, orien);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 计算角度
     *
     * @param degree
     * @return
     */
    static int calculateDegree(int degree) {
        if (degree < 0) {
            degree %= 360;
            degree += 360;
        }
        return Math.round(degree / 90f) * 90;
    }

    /**
     * 创建横竖旋转动画---三种组合旋转(顺转，逆转，顺转180)
     *
     * @param type  0-顺转90 1-逆转90 2-顺转180
     * @param orien 最终所处的方向
     */
    private static void startRotateAnimation(final IRotateView view, int type, final Orientation orien) {
        RotateAnimationListener l = new RotateAnimationListener(view, orien);
        RotateAnimation anim = RotateViewUtils.createRotateAnimation(type, l);
        view.startAnimation(anim);
    }

    /**
     * 创建旋转动画
     *
     * @param type
     * @param l
     * @return
     */
    private static RotateAnimation createRotateAnimation(int type, Animation.AnimationListener l) {
        float toDegrees = 90;
        switch (type) {
            case RotateViewUtils.ANIM_TYPE_CLOCKWISE_90:
                toDegrees = 90;
                break;
            case RotateViewUtils.ANIM_TYPE_ANTICLOCKWISE_90:
                toDegrees = -90;
                break;
            case RotateViewUtils.ANIM_TYPE_CLOCKWISE_180:
                toDegrees = 180;
                break;
            default:
                break;
        }
        float fromDegrees = 0;
        RotateAnimation anim =
                new RotateAnimation(fromDegrees, toDegrees, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(false);
        anim.setDuration(200);
        anim.setAnimationListener(l);
        return anim;
    }

    private static class RotateAnimationListener implements Animation.AnimationListener {

        private IRotateView mView;
        private Orientation mOrien;

        public RotateAnimationListener(IRotateView view, Orientation orien) {
            mView = view;
            mOrien = orien;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mView.clearAnimation();
            mView.requestOrientation(mOrien);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

}
