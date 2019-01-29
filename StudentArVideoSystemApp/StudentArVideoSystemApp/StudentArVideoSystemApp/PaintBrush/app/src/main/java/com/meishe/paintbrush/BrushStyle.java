package com.meishe.paintbrush;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

/**
 * 画笔样式类
 */
@Keep
public class BrushStyle implements Parcelable {
    public int id;
    public Drawable drawable;
    public String fill_mode;
    public float stroke_width;
    public boolean stroke_animated;
    public float stroke_animation_speed;
    public float stroke_analog_amplitude;
    public int stroke_cap_style;
    public int stroke_jone_style;
    public int stroke_analog_type;
    public int stroke_analog_period;
    public int stroke_texture_warp_type;

    public BrushStyle() {
        stroke_animated = false;
        stroke_width = 0.05f;
        stroke_analog_amplitude = 0.5f;
        stroke_analog_period = 3000;
        stroke_animation_speed = 10;
    }

    protected BrushStyle(Parcel in) {
        id = in.readInt();
        fill_mode = in.readString();
        stroke_width = in.readFloat();
        stroke_animated = in.readByte() != 0;
        stroke_animation_speed = in.readFloat();
        stroke_analog_amplitude = in.readFloat();
        stroke_cap_style = in.readInt();
        stroke_jone_style = in.readInt();
        stroke_analog_type = in.readInt();
        stroke_analog_period = in.readInt();
        stroke_texture_warp_type = in.readInt();
    }

    public static final Creator<BrushStyle> CREATOR = new Creator<BrushStyle>() {
        @Override
        public BrushStyle createFromParcel(Parcel in) {
            return new BrushStyle(in);
        }

        @Override
        public BrushStyle[] newArray(int size) {
            return new BrushStyle[size];
        }
    };

    public BrushStyle clone() {
        BrushStyle data_clone = new BrushStyle();
        data_clone.id = this.id;
        data_clone.drawable = this.drawable;
        data_clone.fill_mode = this.fill_mode;
        data_clone.stroke_animated = this.stroke_animated;
        data_clone.stroke_analog_amplitude = this.stroke_analog_amplitude;
        data_clone.stroke_width = this.stroke_width;
        data_clone.stroke_cap_style = this.stroke_cap_style;
        data_clone.stroke_analog_type = this.stroke_analog_type;
        data_clone.stroke_jone_style = this.stroke_jone_style;
        data_clone.stroke_analog_period = this.stroke_analog_period;
        data_clone.stroke_animation_speed = this.stroke_animation_speed;
        data_clone.stroke_texture_warp_type = this.stroke_texture_warp_type;
        return data_clone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(fill_mode);
        parcel.writeFloat(stroke_width);
        parcel.writeByte((byte) (stroke_animated ? 1 : 0));
        parcel.writeFloat(stroke_animation_speed);
        parcel.writeFloat(stroke_analog_amplitude);
        parcel.writeInt(stroke_cap_style);
        parcel.writeInt(stroke_jone_style);
        parcel.writeInt(stroke_analog_type);
        parcel.writeInt(stroke_analog_period);
        parcel.writeInt(stroke_texture_warp_type);
    }

    @Override
    public String toString() {
        return "BrushStyle{" +
                "id=" + id +
                ", drawable=" + drawable +
                ", fill_mode='" + fill_mode + '\'' +
                ", stroke_width=" + stroke_width +
                ", stroke_animated=" + stroke_animated +
                ", stroke_animation_speed=" + stroke_animation_speed +
                ", stroke_analog_amplitude=" + stroke_analog_amplitude +
                ", stroke_cap_style=" + stroke_cap_style +
                ", stroke_jone_style=" + stroke_jone_style +
                ", stroke_analog_type=" + stroke_analog_type +
                ", stroke_analog_period=" + stroke_analog_period +
                ", stroke_texture_warp_type=" + stroke_texture_warp_type +
                '}';
    }
}