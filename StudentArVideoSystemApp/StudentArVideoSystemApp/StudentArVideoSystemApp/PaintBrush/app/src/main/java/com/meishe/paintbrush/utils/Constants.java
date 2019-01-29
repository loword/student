package com.meishe.paintbrush.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.meishe.paintbrush.BrushColor;
import com.meishe.paintbrush.BrushStyle;
import com.meishe.paintbrush.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ms on 2018-10-12.
 */

public class Constants {
    public static final int FROMMAINACTIVITYTOVISIT = 1001;//从主页面进入视频选择页面
    public static final int FROMCLIPEDITACTIVITYTOVISIT = 1002;//从片段编辑页面进入视频选择页面
    public static final int FROMPICINPICACTIVITYTOVISIT = 1003;//从画中画面进入视频选择页面

    //视音频音量值
    public static final float VIDEOVOLUME_DEFAULTVALUE = 2.0f;
    public static final float VIDEOVOLUME_MAXVOLUMEVALUE = 8.0f;
    public static final int VIDEOVOLUME_MAXSEEKBAR_VALUE = 100;

    /**画笔类型*/
    public static final int ITEM_TYPE_BRUSH_COLOR = 1;
    public static final int ITEM_TYPE_BRUSH_STYLE = 2;

    /**
     * 获取json文件记录的画笔类型
     */
    public static Map<Integer, BrushStyle> listBrushStyleFromJson(Context context) {
        Map<Integer, BrushStyle> brushStyleMap = new TreeMap<>();
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("brush/brush_style.json"),"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = br.readLine()) != null){
                builder.append(line);
            }
            br.close();
            isr.close();
            JSONObject data = new JSONObject(builder.toString());
            JSONArray array = data.getJSONArray("brush_style");
            for (int i = 0; i < array.length(); i++) {
                JSONObject role = array.getJSONObject(i);
                BrushStyle brushStyle = new BrushStyle();
                brushStyle.id = role.getInt("id");
                brushStyle.fill_mode = role.getString("fill_mode");
                brushStyle.stroke_width = (float) role.getDouble("stroke_width");
                brushStyle.stroke_analog_amplitude = (float) role.getDouble("stroke_analog_amplitude");
                brushStyle.stroke_animated = role.getBoolean("stroke_animated");
                brushStyle.stroke_cap_style = role.getInt("stroke_cap_style");
                brushStyle.stroke_analog_type = role.getInt("stroke_analog_type");
                brushStyle.stroke_analog_period = role.getInt("stroke_analog_period");
                brushStyle.stroke_jone_style = role.getInt("stroke_jone_style");
                brushStyle.stroke_texture_warp_type = role.getInt("stroke_texture_warp_type");
                brushStyle.stroke_animation_speed = (float) role.getDouble("stroke_animation_speed");
                if(brushStyle.id == 0) {
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_1);
                } else if(brushStyle.id == 1){
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_2);
                } else if(brushStyle.id == 2){
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_3);
                } else if(brushStyle.id == 3){
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_4);
                } else if(brushStyle.id == 4){
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_5);
                }
                brushStyleMap.put(brushStyle.id, brushStyle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return brushStyleMap;
    }

    /**
     * 获取json文件记录的画笔颜色
     */
    public static Map<Integer, BrushColor> listBrushColorFromJson(Context context) {
        Map<Integer, BrushColor> brushColorMap = new TreeMap<>();
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("brush/brush_color.json"),"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = br.readLine()) != null){
                builder.append(line);
            }
            br.close();
            isr.close();
            JSONObject data = new JSONObject(builder.toString());
            JSONArray array = data.getJSONArray("brush_color");
            for (int i = 0; i < array.length(); i++) {
                JSONObject role = array.getJSONObject(i);
                BrushColor brushColor = new BrushColor();
                brushColor.id = role.getInt("id");
                brushColor.color = Color.parseColor(role.getString("color"));
                brushColor.color_caf_path1 = role.getString("color_caf_path1");
                brushColor.color_caf_path2 = role.getString("color_caf_path2");
                brushColor.color_caf_width1 = role.getInt("color_caf_width1");
                brushColor.color_caf_height1 = role.getInt("color_caf_height1");
                brushColor.color_caf_width2 = role.getInt("color_caf_width2");
                brushColor.color_caf_height2 = role.getInt("color_caf_height2");
                brushColorMap.put(brushColor.id, brushColor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return brushColorMap;
    }
}
