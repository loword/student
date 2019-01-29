package com.meishe.paintbrush.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class TouchAreaLayout extends RelativeLayout {
    public TouchAreaLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchAreaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchAreaLayout(Context context) {
        super(context);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
