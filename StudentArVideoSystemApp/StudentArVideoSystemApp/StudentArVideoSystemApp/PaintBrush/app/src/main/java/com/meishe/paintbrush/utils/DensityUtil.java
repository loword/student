package com.meishe.paintbrush.utils;

import android.content.Context;

/**
 * Created by ms on 2018/10/12.
 */

public class DensityUtil {

    /**
     * dp转换成px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
