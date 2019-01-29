package com.meishe.paintbrush;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * 画笔颜色类
 */
@Keep
public class BrushColor implements Serializable {
    public int id;
    public int color;
    public String color_caf_path1;
    public String color_caf_path2;
    public int color_caf_width1;
    public int color_caf_height1;
    public int color_caf_width2;
    public int color_caf_height2;

    public BrushColor() {
    }

    public BrushColor clone() {
        BrushColor data_clone = new BrushColor();
        data_clone.id = this.id;
        data_clone.color = this.color;
        data_clone.color_caf_path1 = this.color_caf_path1;
        data_clone.color_caf_path2 = this.color_caf_path2;
        data_clone.color_caf_width1 = this.color_caf_width1;
        data_clone.color_caf_height1 = this.color_caf_height1;
        data_clone.color_caf_width2 = this.color_caf_width2;
        data_clone.color_caf_height2 = this.color_caf_height2;
        return data_clone;
    }
}