/**
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.ui.rotateview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.baidu.ar.rotate.Orientation;

/**
 * 支持布局旋转及子视图的事件分发处理 仅支持90度的倍数旋转 仅对第一个子视图做了处理
 */
public class RotateViewGroup extends ViewGroup implements IRotateView {

    /**
     * angle等于这个数时，传入的参数视为无效，主要使用在集合视图中，由于cell重建需要用到它
     */
    private int mAngle;

    /**
     * 是否支持复用，集合视图中，效率会低一点
     */
    private boolean mFlag;

    private final Matrix mRotateMatrix = new Matrix();

    private final Rect mViewRectRotated = new Rect();

    private final RectF mTempRectF1 = new RectF();

    private final RectF mTempRectF2 = new RectF();

    private final float[] mViewTouchPoint = new float[2];

    private final float[] mChildTouchPoint = new float[2];

    private boolean mAngleChanged = true;

    public RotateViewGroup(Context context) {
        this(context, null);
    }

    public RotateViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final View view = getView();
        if (view != null) {
            final LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (!mFlag) {
                if (mAngle != layoutParams.angle) {
                    // 首次使用布局中得转角
                    mAngle = layoutParams.angle;
                    mAngleChanged = true;
                }
            } else {
                if (mAngle != layoutParams.angle) {
                    layoutParams.angle = mAngle;
                    mAngleChanged = true;
                }
            }
            if (Math.abs(mAngle % 180) == 90) {
                measureChild(view, heightMeasureSpec, widthMeasureSpec);
                setMeasuredDimension(View.resolveSize(view.getMeasuredHeight(), widthMeasureSpec),
                        View.resolveSize(view.getMeasuredWidth(), heightMeasureSpec));
            } else {
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(View.resolveSize(view.getMeasuredWidth(), widthMeasureSpec),
                        View.resolveSize(view.getMeasuredHeight(), heightMeasureSpec));
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mAngleChanged || changed) {
            final RectF layoutRect = mTempRectF1;
            final RectF layoutRectRotated = mTempRectF2;
            layoutRect.set(0, 0, r - l, b - t);
            float rotateAngle = mAngle;
            if (mAngle == 270 || mAngle == 90) {
                // 子控件旋转的角度为屏幕方向的角度
                rotateAngle = 360 - mAngle;
            }
            mRotateMatrix.setRotate(rotateAngle, layoutRect.centerX(), layoutRect.centerY());
            mRotateMatrix.mapRect(layoutRectRotated, layoutRect);
            layoutRectRotated.round(mViewRectRotated);
            mAngleChanged = false;
        }

        final View view = getView();
        if (view != null) {
            view.layout(mViewRectRotated.left, mViewRectRotated.top, mViewRectRotated.right, mViewRectRotated.bottom);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(mAngle, getWidth() / 2f, getHeight() / 2f);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        invalidate();
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mViewTouchPoint[0] = event.getX();
        mViewTouchPoint[1] = event.getY();
        mRotateMatrix.mapPoints(mChildTouchPoint, mViewTouchPoint);
        event.setLocation(mChildTouchPoint[0], mChildTouchPoint[1]);
        boolean result = super.dispatchTouchEvent(event);
        event.setLocation(mViewTouchPoint[0], mViewTouchPoint[1]);
        return result;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int angle;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.angle = RotateViewUtils.calculateDegree(0);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
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
        mFlag = true;
        requestLayout();
    }

    @Override
    public int getAngle() {
        return mAngle;
    }

    /**
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

    /**
     * 获取子控件
     *
     * @return
     */
    private View getView() {
        return getChildAt(0);
    }

}
