package com.meishe.paintbrush.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.meishe.paintbrush.R;

/**
 * 字幕样式之颜色选择控件
 */
public class RoundColorView extends View {
    private final String TAG = "RoundColorView";
    private Context mContext;
    private Paint mPaintOut, mPaintIn;
    private float out_radius = 0, in_radius = 0;
    private int COLOR_CIRCLE = Color.RED;
    private int COLOR_BORDER = Color.WHITE;

    public RoundColorView(Context context) {
        super(context);
        init(context);
    }

    public RoundColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MSFontColorView);
            COLOR_CIRCLE = a.getColor(R.styleable.MSFontColorView_color, Color.RED);
        }

        init(context);
    }


    private void init(Context context) {
        this.mContext = context;
        mPaintOut = new Paint();
        mPaintOut.setAntiAlias(true);
        mPaintIn = new Paint();
        mPaintIn.setAntiAlias(true);
        out_radius = DensityUtil.dip2px(mContext, 15);
        in_radius = DensityUtil.dip2px(mContext, 14);
    }

    public void setColor(int color) {
        COLOR_CIRCLE = color;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaintOut.setStyle(Paint.Style.FILL);
        mPaintOut.setColor(COLOR_BORDER);
        canvas.drawCircle(out_radius, out_radius, out_radius, mPaintOut);

        mPaintIn.setStyle(Paint.Style.FILL);
        mPaintIn.setColor(COLOR_CIRCLE);
        canvas.drawCircle(out_radius, out_radius, in_radius, mPaintIn);
    }
}