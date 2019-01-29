package com.meishe.arface2.beauty_shape;

import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * Created by ms on 2018/8/9.
 */
@Keep
public class BeautyShapeListItem implements Serializable {
    public String nameCH;
    public boolean selected;
    public Drawable image_drawable;

    public BeautyShapeListItem() {
        selected = false;
    }
}
