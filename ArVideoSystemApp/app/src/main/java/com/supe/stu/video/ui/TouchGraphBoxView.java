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

import com.baidu.ar.util.Utils;

import java.util.List;

public class TouchGraphBoxView extends View {

    private RectF faceRectf;
    private Context mContext;

    int padding = 0;

    public TouchGraphBoxView(Context context) {
        super(context);
        mContext = context;
        setWillNotDraw(false);
        padding = Utils.dipToPx(mContext, 50);
    }

    public TouchGraphBoxView(Context context,
                             @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setWillNotDraw(false);
        padding = Utils.dipToPx(mContext, 50);
    }

    public TouchGraphBoxView(Context context,
                             @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setWillNotDraw(false);
        padding = Utils.dipToPx(mContext, 50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceRectf == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 消除锯齿
        paint.setStrokeWidth(2); // 设置画笔的宽度
        paint.setStyle(Paint.Style.STROKE); // 设置绘制轮廓
        paint.setColor(Color.YELLOW); // 设置颜色
        RectF rectF = faceRectf;
        canvas.drawRect(rectF, paint);

    }

    List<PointF> faceAreas;

    public void setRectF(RectF faceRectf) {

        if (faceRectf == null || faceRectf.right <= 0) {
            return;
        }
        this.faceRectf = faceRectf;
        this.invalidate();
    }


    public void drawRect(int x, int y) {
        this.faceRectf = new RectF(x - padding, y - padding,
                x + padding, y + padding);
        this.setVisibility(View.VISIBLE);
        this.invalidate();
    }
}
