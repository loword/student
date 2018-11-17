/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.supe.stu.video.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.ar.ARController;
import com.baidu.ar.DuMixSource;
import com.baidu.ar.DuMixTarget;

import java.util.List;

public class FaceBoxView extends View {

    private RectF faceRectf;
    private ARController mARController;

    boolean isFaceBox;
    boolean isFrontCamera = true;

    public FaceBoxView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public FaceBoxView(Context context,
                       @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public FaceBoxView(Context context,
                       @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public void switchCamera(boolean isFrontCamera) {
        this.isFrontCamera = isFrontCamera;
        faceRectf = null;
    }

    public void setARController(ARController arController) {
        mARController = arController;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceRectf == null) {
            return;
        }

        drawFrontFace(canvas);
    }

    private void drawFrontFace(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 消除锯齿
        paint.setStrokeWidth(2); // 设置画笔的宽度
        paint.setStyle(Paint.Style.STROKE); // 设置绘制轮廓
        paint.setColor(Color.YELLOW); // 设置颜色
        RectF rectF = new RectF(faceRectf.left, faceRectf.top, faceRectf.right, faceRectf.bottom);
        canvas.drawRect(rectF, paint);
    }

    public void setRectF(RectF faceRectf) {

        if (faceRectf == null || faceRectf.right <= 0) {
            return;
        }
        this.faceRectf = faceRectf;
        this.invalidate();
    }

    List<PointF> faceAreas;

    public void findFaceArea(List<PointF> faceAreas, boolean istrackinged) {
        isFaceBox = true;
        if (istrackinged) {
            if (faceAreas == null || faceAreas.size() <= 0) {
                return;
            }
            this.faceAreas = faceAreas;
            PointF minXItem = null;
            PointF maxXItem = null;
            PointF minYItem = null;
            PointF maxYItem = null;

            if (minXItem == null) {
                minXItem = faceAreas.get(0);
                maxXItem = faceAreas.get(0);
                minYItem = faceAreas.get(0);
                maxYItem = faceAreas.get(0);
            }
            for (int i = 0; i < faceAreas.size(); i++) {

                if (minXItem.x > faceAreas.get(i).x) {
                    minXItem = faceAreas.get(i);
                }

                if (maxXItem.x < faceAreas.get(i).x) {
                    maxXItem = faceAreas.get(i);
                }

                if (minYItem.y > faceAreas.get(i).y) {
                    minYItem = faceAreas.get(i);
                }

                if (maxYItem.y < faceAreas.get(i).y) {
                    maxYItem = faceAreas.get(i);
                }

            }

            float left = minXItem.x;
            float top = minYItem.y;

            float right = maxXItem.x;
            float buttom = maxYItem.y;
            RectF rectF;
            if (isFrontCamera) {
                PointF lt = convertFacePoint(new PointF(left, top));
                PointF rb = convertFacePoint(new PointF(right, buttom));
                if (lt == null || rb == null) {
                    return;
                }
                rectF = new RectF(rb.x, rb.y, lt.x, lt.y);
            } else {
                PointF lt = convertFacePoint(new PointF(right, top));
                PointF rb = convertFacePoint(new PointF(left, buttom));
                if (lt == null || rb == null) {
                    return;
                }
                rectF = new RectF(rb.x, rb.y, lt.x, lt.y);
            }

            setRectF(rectF);
        }
    }

    public PointF getCenterPoint() {
        if (faceRectf == null) {
            return null;
        }
        return new PointF(faceRectf.centerX(), faceRectf.centerY());
    }

    private PointF convertFacePoint(PointF point) {
        if (mARController == null) {
            return null;
        }
        DuMixSource source = mARController.getDuMixSource();
        PointF sourceP;
        if (isFrontCamera) {
            sourceP = new PointF(source.getSourceHeight() - point.y,
                    source.getSourceWidth() - point.x);
        } else {
            sourceP = new PointF(source.getSourceHeight() - point.y, point.x);
        }
        float radio = mARController.getInputRatio();
        DuMixTarget target = mARController.getDuMixTarget();
        PointF targetP = new PointF(sourceP.x * radio, sourceP.y * radio);
        float tx = (source.getSourceHeight() * radio - target.getTargetWidth()) / 2;
        float ty = (source.getSourceWidth() * radio - target.getTargetHeight()) / 2;
        PointF res = new PointF(targetP.x - tx, targetP.y - ty);
        return res;
    }
}