package com.meishe.paintbrush.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.meicam.sdk.NvsColor;
import com.meicam.sdk.NvsSize;
import com.meicam.sdk.NvsStreamingContext;
import com.meishe.paintbrush.BrushColor;
import com.meishe.paintbrush.BrushStyle;
import com.meishe.paintbrush.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;



public class Util {

    /**
     * 获取json文件记录的画笔类型
     */
    public static Map<Integer, BrushStyle> listBrushStyleFromJson(Context context) {
        Map<Integer, BrushStyle> brushStyleMap = new TreeMap<>();
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("brush/brush_style.json"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
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
                if (brushStyle.id == 0) {
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_1);
                } else if (brushStyle.id == 1) {
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_2);
                }
                else if (brushStyle.id == 2) {
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_3);
                } else if (brushStyle.id == 3) {
                    brushStyle.drawable = ContextCompat.getDrawable(context, R.mipmap.brush_icon_4);
                }
                else if (brushStyle.id == 4) {
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
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("brush/brush_color.json"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
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

    /**
     * 将dp值转换成px值
     *
     * @param context context
     * @param dpValue dp值
     * @return 转换后的px值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /*将int颜色值转换为ARGB*/
    public static NvsColor convertHexToRGB(int hexColocr) {
        NvsColor color = new NvsColor(0, 0, 0, 0);
        color.a = (float) ((hexColocr & 0xff000000) >>> 24) / 0xFF;
        color.r = (float) ((hexColocr & 0x00ff0000) >> 16) / 0xFF;
        color.g = (float) ((hexColocr & 0x0000ff00) >> 8) / 0xFF;
        color.b = (float) ((hexColocr) & 0x000000ff) / 0xFF;
        return color;
    }

    /**
     *   获取当前时间，转换成字符串 格式：yyyyMMddHHmmss" 年月日小时分钟秒
     * @return
     */
    public static String getCharacterAndNumber() {
        String rel = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        rel = formatter.format(curDate);
        return rel;
    }

    /**
     * 对rgb色彩加入透明度
     * @param alpha     透明度，取值范围 0.0f -- 1.0f.
     * @param baseColor
     * @return a color with alpha made from base color
     */
    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    /**
     *  检测手机状态
     * @return
     */
    public static int checkMobileModel() {
        int m_compileFlag = 0;
        String model = Build.MODEL;
        String manufaturer = Build.MANUFACTURER;


        if(model == null || manufaturer == null) {
            return 0;
        }
        model = model.toUpperCase();
        manufaturer = manufaturer.toUpperCase();
        if((model.equals("REDMI 6") && manufaturer.equals("XIAOMI"))) {
            m_compileFlag |= NvsStreamingContext.STREAMING_ENGINE_COMPILE_FLAG_DISABLE_HARDWARE_ENCODER;
        }

        return m_compileFlag;
    }

    public static void showDialog(Context context, final String title, final String first_tip) {
        final CommonDialog dialog = new CommonDialog(context, 1);
        dialog.setOnCreateListener(new CommonDialog.OnCreateListener() {
            @Override
            public void OnCreated() {
                dialog.setTitleTxt(title);
                dialog.setFirstTipsTxt(first_tip);
            }
        });
        dialog.setOnBtnClickListener(new CommonDialog.OnBtnClickListener() {
            @Override
            public void OnOkBtnClicked(View view) {
                dialog.dismiss();
            }

            @Override
            public void OnCancelBtnClicked(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 得到LiveWindow的实际大小
     * @param timelineSize
     * @param parentSize
     * @param fullScreen
     * @return
     */
    public static NvsSize getLiveWindowSize(NvsSize timelineSize, NvsSize parentSize, boolean fullScreen) {
        if (timelineSize.height > timelineSize.width && fullScreen) {
            return parentSize;
        }

        NvsSize size = new NvsSize(timelineSize.width, timelineSize.height);

        float scaleWidth = (float) parentSize.width / timelineSize.width;
        float scaleHeight = (float) parentSize.height / timelineSize.height;
        if (scaleWidth < scaleHeight) {
            int width = parentSize.width;
            size.height = width * size.height / size.width;
            size.width = width;
            if (size.height > parentSize.height) {
                size.height = parentSize.height;
            }
        } else {
            int height = parentSize.height;
            size.width = height * size.width / size.height;
            size.height = height;

            if (size.width > parentSize.width) {
                size.width = parentSize.width;
            }
        }
        return size;
    }
    /**
     *  获取屏幕宽度
     * @param context  context
     * @return  屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        return screenWidth;
    }
}