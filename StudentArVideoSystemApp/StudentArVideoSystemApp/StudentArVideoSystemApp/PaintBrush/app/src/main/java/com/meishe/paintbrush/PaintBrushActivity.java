package com.meishe.paintbrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meicam.sdk.NvsAVFileInfo;
import com.meicam.sdk.NvsAudioResolution;
import com.meicam.sdk.NvsAudioTrack;
import com.meicam.sdk.NvsLiveWindow;
import com.meicam.sdk.NvsPaintingEffectContext;
import com.meicam.sdk.NvsRational;
import com.meicam.sdk.NvsSize;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsTimelineVideoFx;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoResolution;
import com.meicam.sdk.NvsVideoStreamInfo;
import com.meicam.sdk.NvsVideoTrack;
import com.meishe.paintbrush.base.BaseActivity;
import com.meishe.paintbrush.utils.BrushAdapter;
import com.meishe.paintbrush.utils.ColorSeekBar;
import com.meishe.paintbrush.utils.Constants;
import com.meishe.paintbrush.utils.CustomTitleBar;
import com.meishe.paintbrush.utils.LiveWindow;
import com.meishe.paintbrush.utils.MediaScannerUtil;
import com.meishe.paintbrush.utils.OnTitleBarClickListener;
import com.meishe.paintbrush.utils.ParameterSettingValues;
import com.meishe.paintbrush.utils.ToastUtil;
import com.meishe.paintbrush.utils.Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class PaintBrushActivity extends BaseActivity implements NvsStreamingContext.PlaybackCallback, NvsStreamingContext.PlaybackCallback2, NvsStreamingContext.StreamingEngineCallback {
    private static final String TAG = "PaintBrushActivity";
    private final String PAINT_FX_NAME = "Painting Effect";
    private CustomTitleBar m_titleBar;
    private Context m_context;
    private LiveWindow m_liveWindow;
    private ImageButton m_editBrushBtn;
    private LinearLayout m_operateLayout;
    private String m_filePath;
    private NvsStreamingContext m_streamingContext;
    private int m_imgWidth;
    private int m_imgHeight;
    private NvsTimeline m_timeline;
    private NvsVideoTrack m_videoTrack;
    private NvsAudioTrack m_originAudioTrack;
    // 画刷类型
    private Map<Integer, BrushStyle> m_brushStyleMap;
    private RecyclerView m_brushStyleRv;
    private BrushAdapter m_brushStyleAdapter;
    private RecyclerView m_brushColorRv;
    private Map<Integer, BrushColor> m_brushColorMap;
    private BrushAdapter m_brushColorAdapter;
    private float m_currentScreenX, m_currentScreenY;
    private NvsTimelineVideoFx m_paintingVideoFx;
    private NvsPaintingEffectContext m_paintingEffectContext;
    private BrushStyle m_brushStyle;
    private ImageButton m_brushUndoBtn;
    // 时间线特效
    private List<NvsTimelineVideoFx> m_fxList = new ArrayList<>();
    // 颜色_文理
    private BrushColor m_brushTextureColor;
    // 颜色_纯色
    private ColorSeekBar m_selectColorSB;
    private int m_brushColor = 0xFFFF0000;
    //alpha
    private SeekBar m_AlphaDegreeSb;
    private float progressF = 1.0F;
    // thick
    private SeekBar m_ThickDegreeSb;
    private float thicProgressF = 0.01F;
    // 进度
    private RelativeLayout m_waitLayout;
    // 取消生成视频
    boolean isCanceled = false;
    // 视频文件生成目录
    private String mCompileVideoPath;

    // 导出窗口
    private RelativeLayout mCompilePage;
    private ProgressBar mCompileProgressBar;
    private TextView mCompileProgressVal;

    // 设置框触摸位置
    float mPosY = 0.0f;
    float mCurPosY = 0.0f;

    //Titlebar返回键处理结果
    boolean result;
    int[] resId = {R.mipmap.brush_icon_1, R.mipmap.brush_icon_2, R.mipmap.brush_icon_3, R.mipmap.brush_icon_4, R.mipmap.brush_icon_5};
    private ImageView mCompileCancel;

    private TextView mAlphaDegreeText;
    private TextView mThickDegreeText;

    private DecimalFormat decimalFormatOfThick = new DecimalFormat("##0.00");
    private DecimalFormat decimalFormatOfAlpha = new DecimalFormat("##0.0");
    private RelativeLayout m_select_layout;
    private View m_touch_area_layout;
    private RelativeLayout mWrapper;

    @Override
    protected int initRootView() {
        m_context = this;
        m_streamingContext = NvsStreamingContext.getInstance();
        m_streamingContext.setPlaybackCallback(this);
        m_streamingContext.setStreamingEngineCallback(this);
        m_streamingContext.setPlaybackCallback2(this);
        Intent intent = getIntent();
        m_filePath = intent.getStringExtra("path");
        return R.layout.activity_brush_paint;
    }

    @Override
    protected void initViews() {
        m_select_layout = (RelativeLayout) findViewById(R.id.select_layout);
        m_touch_area_layout = findViewById(R.id.touch_area_layout);

        m_titleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        m_liveWindow = (LiveWindow) findViewById(R.id.live_window);
        m_editBrushBtn = (ImageButton) findViewById(R.id.edit_brush_btn);
        m_operateLayout = (LinearLayout) findViewById(R.id.operate_layout);
        m_brushStyleRv = (RecyclerView) findViewById(R.id.rv_brush_style);
        m_brushColorRv = (RecyclerView) findViewById(R.id.rv_brush_color);
        m_brushUndoBtn = (ImageButton) findViewById(R.id.brush_undo_btn);
        m_brushUndoBtn.setAlpha(0.5f);
        m_selectColorSB = (ColorSeekBar) findViewById(R.id.select_color_seek_bar);
        m_selectColorSB.setProgress(50);
        m_AlphaDegreeSb = (SeekBar) findViewById(R.id.alpha_degree_sb);
        m_AlphaDegreeSb.setProgress(100);
        m_ThickDegreeSb = (SeekBar) findViewById(R.id.thick_degree_sb);
        m_ThickDegreeSb.setProgress(1);
        m_waitLayout = (RelativeLayout) findViewById(R.id.waitLayout);
        mCompilePage = (RelativeLayout) findViewById(R.id.compilePage);
        mCompileCancel = (ImageView) findViewById(R.id.compileCancel);
        mCompileProgressBar = (ProgressBar) findViewById(R.id.compileProgressBar);
        mCompileProgressBar.setMax(100);
        mCompileProgressVal = (TextView) findViewById(R.id.compileProgressVal);

        mAlphaDegreeText = (TextView) findViewById(R.id.alpha_degree_text);
        mThickDegreeText = (TextView) findViewById(R.id.thick_degree_text);
        mWrapper = (RelativeLayout) findViewById(R.id.wrapper);
        // 初始化画刷
        initBrushStyleList();
        // 初始化颜色_纹理
        initBrushTextureColorList();
    }

    private void initBrushTextureColorList() {
        m_brushColorMap = Util.listBrushColorFromJson(this);
        if (m_brushColorMap != null && !m_brushColorMap.isEmpty()) {
            m_brushTextureColor = m_brushColorMap.get(0); // 默认纹理
        }
        LinearLayoutManager lm_color = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        m_brushColorRv.setLayoutManager(lm_color);
        m_brushColorRv.addItemDecoration(new SpacesItemDecoration(13));
        m_brushColorAdapter = new BrushAdapter(this, Constants.ITEM_TYPE_BRUSH_COLOR);
        m_brushColorAdapter.updateBrushColorData(m_brushColorMap);
        m_brushColorRv.setAdapter(m_brushColorAdapter);
        m_brushColorAdapter.setOnSelectColorListener(new BrushAdapter.OnSelectColorListener() {
            @Override
            public void onItemSelected(int position, BrushColor itemData) {
                if (itemData == null) {
                    return;
                }
                m_brushTextureColor = itemData;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_brushStyleMap != null && !m_brushStyleMap.isEmpty()) {
            for (BrushStyle mBrushStyle : m_brushStyleMap.values()) {
                mBrushStyle.drawable.clearColorFilter();
            }
            m_brushStyleMap.clear();
            m_brushStyleMap = null;
        }
    }

    private void initBrushStyleList() {
        m_brushStyleMap = Util.listBrushStyleFromJson(this);
        if (m_brushStyleMap != null && !m_brushStyleMap.isEmpty()) {
            m_brushStyle = m_brushStyleMap.get(0); // 默认笔形
            m_brushStyle.drawable = getDrawable(m_brushStyle.drawable, Color.parseColor("#82ACDD"));
        }
        LinearLayoutManager lm_Style = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        m_brushStyleRv.setLayoutManager(lm_Style);
        m_brushStyleAdapter = new BrushAdapter(this, Constants.ITEM_TYPE_BRUSH_STYLE);
        m_brushStyleAdapter.updateBrushStyleData(m_brushStyleMap);
        m_brushStyleRv.addItemDecoration(new SpacesItemDecoration(15));
        m_brushStyleRv.setAdapter(m_brushStyleAdapter);
        m_brushStyleAdapter.notifyDataSetChanged();
        m_brushStyleAdapter.setOnSelectStyleListener(new BrushAdapter.OnSelectStyleListener() {
            @Override
            public void onItemSelected(int position, BrushStyle itemData) {
                if (itemData == null) {
                    return;
                }
                m_brushStyle = itemData;
                if (position == 0 || position == 1 || position == 4) {
                    // 纯颜色
                    m_selectColorSB.setVisibility(View.VISIBLE);
                    m_brushColorRv.setVisibility(View.GONE);
                } else {
                    //文理色
                    m_selectColorSB.setVisibility(View.GONE);
                    m_brushColorRv.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "onItemSelected: " + position + itemData.toString());
                int index = 0;
                for (BrushStyle mBrushStyle : m_brushStyleMap.values()) {
                    mBrushStyle.drawable.clearColorFilter();
                }
                itemData.drawable = getDrawable(itemData.drawable, Color.parseColor("#82ACDD"));
                m_brushStyleAdapter.notifyDataSetChanged();
            }
        });
    }

    public Drawable getDrawable(Drawable drawable, int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        return drawable;
    }

    @Override
    protected void initTitle() {
        m_titleBar.setTextCenter(R.string.brush);
        m_titleBar.setTextRight(R.string.compile);
        m_titleBar.setTextRightVisible(View.VISIBLE);
    }

    @Override
    protected void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 创建时间线
                createTimeline();
                playVideo(0, m_timeline.getDuration());
            }
        }, 200);

        // 撤销按钮置灰
        if (!haveEditTimeline()) {
            m_brushUndoBtn.setAlpha(0.5f);
        }
    }

    private void createTimeline() {
        if (m_streamingContext == null)
            return;
        if (m_timeline != null) {
            m_streamingContext.removeTimeline(m_timeline);
            m_timeline = null;
        }
        if (m_filePath == null)
            return;
        NvsAVFileInfo avFileInfo = m_streamingContext.getAVFileInfo(m_filePath);
        if (avFileInfo == null) {
            Log.e(TAG, "NvsAVFileInfo is null!");
            return;
        }
        m_imgWidth = avFileInfo.getVideoStreamDimension(0).width;
        m_imgHeight = avFileInfo.getVideoStreamDimension(0).height;
        int rotation = avFileInfo.getVideoStreamRotation(0);

        NvsAudioResolution audioEditRes = new NvsAudioResolution();
        audioEditRes.sampleRate = 44100;
        audioEditRes.channelCount = 2;
        NvsVideoResolution videoEditRes = new NvsVideoResolution();
        videoEditRes.imagePAR = new NvsRational(1, 1);
        NvsRational videoFps = new NvsRational(25, 1);
        videoEditRes.imageWidth = 720;
        videoEditRes.imageHeight = 1280;

        if (m_imgWidth > m_imgHeight) {
            if (rotation == NvsVideoStreamInfo.VIDEO_ROTATION_0 || rotation == NvsVideoStreamInfo.VIDEO_ROTATION_180) {
                videoEditRes.imageWidth = 1280;
                videoEditRes.imageHeight = 720;
            }
        } else {
            if (rotation == NvsVideoStreamInfo.VIDEO_ROTATION_90 || rotation == NvsVideoStreamInfo.VIDEO_ROTATION_270) {
                videoEditRes.imageWidth = 1280;
                videoEditRes.imageHeight = 720;
            }
        }
        m_liveWindow.setFillMode(NvsLiveWindow.FILLMODE_PRESERVEASPECTFIT);
        m_liveWindow.setClickable(true);
        m_timeline = m_streamingContext.createTimeline(videoEditRes, videoFps, audioEditRes);

        m_streamingContext.connectTimelineWithLiveWindow(m_timeline, m_liveWindow);

        m_videoTrack = m_timeline.appendVideoTrack();
        if (null == m_videoTrack) {
            Log.e(TAG, "videoTrack is null!");
            return;
        }

        m_originAudioTrack = m_timeline.appendAudioTrack();
        if (null == m_originAudioTrack) {
            Log.e(TAG, "m_originAudioTrack is null!");
            return;
        }
        // 添加绘制区域
        NvsSize size = new NvsSize(videoEditRes.imageWidth, videoEditRes.imageHeight);
        if (size.width <= size.height) {
            // 初始布局大小
            int width = m_liveWindow.getWidth();
            int height = m_liveWindow.getHeight();
            // 根据视频获得的实际大小
            NvsSize liveWindowSize = Util.getLiveWindowSize(size, new NvsSize(width, height), false);
            // 设置
            ViewGroup.LayoutParams params = m_liveWindow.getLayoutParams();
            params.height = liveWindowSize.height;
            int livewindowWidth = liveWindowSize.height * 9 / 16;
            params.width = livewindowWidth;
            m_liveWindow.setLayoutParams(params);

            ViewGroup.LayoutParams select_params = m_select_layout.getLayoutParams();
            select_params.height = params.height;
            select_params.width = params.width;
            m_select_layout.setLayoutParams(select_params);

            ViewGroup.LayoutParams params2 = m_touch_area_layout.getLayoutParams();
            params2.height = params.height;
            params2.width = params.width;
            m_touch_area_layout.setLayoutParams(params2);
        } else {
            int m_screenWidth = Util.getScreenWidth(getApplicationContext());
            ViewGroup.LayoutParams params = m_liveWindow.getLayoutParams();
            params.width = (int) m_screenWidth;
            params.height = (int) (m_screenWidth * 9 / 16);
            m_liveWindow.setLayoutParams(params);

            ViewGroup.LayoutParams select_params = m_select_layout.getLayoutParams();
            select_params.height = params.height;
            select_params.width = params.width;
            m_select_layout.setLayoutParams(select_params);

            ViewGroup.LayoutParams params2 = m_touch_area_layout.getLayoutParams();
            params2.height = params.height;
            params2.width = params.width;
            m_touch_area_layout.setLayoutParams(params2);
        }
        //  m_touch_area_layout.setVisibility(View.VISIBLE);
        NvsVideoClip clip = m_videoTrack.appendClip(m_filePath);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.close_operate_layout).setOnClickListener(this);
        m_editBrushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditBrush();
            }
        });
        mWrapper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect frame = new Rect();
                m_liveWindow.getHitRect(frame);
                float eventX = event.getX();
                float eventY = event.getY();
                if (!frame.contains((int) eventX, (int) eventY)) {
                    closeEditBrush();
                }
                return true;
            }
        });
        m_liveWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                m_currentScreenX = event.getX();
                m_currentScreenY = event.getY();
                PointF ndc_point = m_liveWindow.mapViewToNormalized(new PointF(m_currentScreenX, m_currentScreenY));
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        closeEditBrush();
                        // 特效
                        m_paintingVideoFx = m_timeline.addBuiltinTimelineVideoFx(0, m_timeline.getDuration(), PAINT_FX_NAME);
                        if (m_paintingVideoFx != null) {
                            m_paintingVideoFx.setAttachment(PAINT_FX_NAME, "1");
                            m_fxList.add(m_paintingVideoFx);
                            // 撤销按钮可用
                            if (haveEditTimeline()) {
                                m_brushUndoBtn.setAlpha(1f);
                                m_brushUndoBtn.setBackground(ContextCompat.getDrawable(m_context, R.mipmap.brush_undo));
                            }
                            // 特效上下文
                            m_paintingEffectContext = m_paintingVideoFx.getPaintingEffectContext();
                            if (m_paintingEffectContext != null) {
                                if (m_brushStyle != null) {
                                    if (m_brushStyle.fill_mode.contains("gradient")) {
                                        m_paintingEffectContext.SetStrokeFillMode(NvsPaintingEffectContext.STROKE_FILL_MODE_GRADIENT);
                                    } else {
                                        m_paintingEffectContext.SetStrokeFillMode(NvsPaintingEffectContext.STROKE_FILL_MODE_TEXTURE);
                                    }
                                    if (m_brushStyle.id == 0 || m_brushStyle.id == 1 || m_brushStyle.id == 4) { // 纯色
                                        m_paintingEffectContext.SetStrokeGradient(Util.convertHexToRGB(Util.getColorWithAlpha(progressF, m_brushColor)));
                                        m_paintingEffectContext.SetStrokeCapStyle(m_brushStyle.stroke_cap_style);
                                        m_paintingEffectContext.SetStrokeJointStyle(m_brushStyle.stroke_jone_style);
                                        m_paintingEffectContext.SetStrokeAnalogType(m_brushStyle.stroke_analog_type);
                                        m_paintingEffectContext.SetStrokeAnalogPeriod(m_brushStyle.stroke_analog_period);
                                        m_paintingEffectContext.SetStrokeAnalogAmplitude(m_brushStyle.stroke_analog_amplitude);
                                        m_paintingEffectContext.SetStrokeAnimated(m_brushStyle.stroke_animated);
                                        m_paintingEffectContext.SetStrokeAnimationSpeed(m_brushStyle.stroke_animation_speed);
                                    } else if (m_brushStyle.id == 2) { //纹理
                                        m_paintingEffectContext.SetStrokeTextureFilePath(m_brushTextureColor.color_caf_path1);
                                        float hor_repeat_times = (float) m_liveWindow.getWidth() / m_brushTextureColor.color_caf_width1;
                                        float ver_repeat_times = (float) m_liveWindow.getHeight() / m_brushTextureColor.color_caf_height1;
                                        m_paintingEffectContext.SetStrokeTextureRepeatTimes((int) hor_repeat_times, (int) ver_repeat_times);
                                        m_paintingEffectContext.SetStrokeTextureWarpType(m_brushStyle.stroke_texture_warp_type);
                                    } else if (m_brushStyle.id == 3) { //纹理
                                        m_paintingEffectContext.SetStrokeTextureFilePath(m_brushTextureColor.color_caf_path2);
                                        float hor_repeat_times = (float) m_liveWindow.getWidth() / m_brushTextureColor.color_caf_width2;
                                        float ver_repeat_times = (float) m_liveWindow.getHeight() / m_brushTextureColor.color_caf_width2;
                                        m_paintingEffectContext.SetStrokeTextureRepeatTimes((int) hor_repeat_times, (int) ver_repeat_times);
                                        m_paintingEffectContext.SetStrokeTextureWarpType(m_brushStyle.stroke_texture_warp_type);
                                    }
                                    m_paintingEffectContext.SetStrokeWidth(thicProgressF);

                                    // 添加绘制起始点
                                    m_paintingEffectContext.AddStroke(new float[]{ndc_point.x, ndc_point.y});
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 添加绘制点
                        if (m_paintingEffectContext != null)
                            m_paintingEffectContext.AppendStroke(new float[]{ndc_point.x, ndc_point.y});
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
        // 后退按钮
        m_titleBar.setOnTitleBarClickListener(new OnTitleBarClickListener() {
            @Override
            public boolean OnBackImageClick() {
                // 返回
                return cancel();
            }

            @Override
            public void OnCenterTextClick() {

            }

            // 生成
            @Override
            public void OnRightTextClick() {
                if (checkIfEdit()) {
                    compileVideo();
                    closeEditBrush();
                } else {
                    Util.showDialog(PaintBrushActivity.this, "友情提示", "请进行画笔操作 再生成");
                }
            }
        });
        // 特效回退
        m_brushUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_timeline != null) {
                    if (m_fxList.size() > 0) {
                        NvsTimelineVideoFx lastVideoFx = m_fxList.get(m_fxList.size() - 1);
                        if (lastVideoFx != null && lastVideoFx.getAttachment(PAINT_FX_NAME) != null) {
                            m_timeline.removeTimelineVideoFx(lastVideoFx);
                        }
                        m_fxList.remove(m_fxList.size() - 1);
                    }
                }
                // 撤销按钮置灰
                if (!haveEditTimeline()) {
                    m_brushUndoBtn.setAlpha(0.5f);
                }
            }
        });

        // colorSeekbar 颜色选择
        m_selectColorSB.setOnStateChangeListener(new ColorSeekBar.OnStateChangeListener() {
            @Override
            public void onColorChanged(int curColor) {
                // 颜色
                m_brushColor = curColor;
                Log.d(TAG, "onColorChanged: " + curColor);
            }

            @Override
            public void onProgressChanged(float progress) {
                // 位置
            }
        });

        // alpha值设置
        m_AlphaDegreeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressF = progress;
                progressF = progressF / 100;
                Log.d(TAG, "onProgressChanged: " + progress);
                Log.d(TAG, "onProgressChangedF:" + progressF);
                mAlphaDegreeText.setText(decimalFormatOfAlpha.format(progressF));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // 画笔宽度设置
        m_ThickDegreeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress++;
                thicProgressF = progress;
                thicProgressF = thicProgressF / 1000;
                if (thicProgressF < 0.02f) thicProgressF = 0.01f;
                mThickDegreeText.setText(decimalFormatOfThick.format(thicProgressF));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // 生成视频
        m_streamingContext.setCompileCallback(new NvsStreamingContext.CompileCallback() {
            @Override
            public void onCompileProgress(NvsTimeline nvsTimeline, int i) {
                updateVideoCompileProgress(i);
            }

            @Override
            public void onCompileFinished(NvsTimeline nvsTimeline) {
                mCompilePage.setVisibility(View.GONE);
                m_streamingContext.setCompileConfigurations(null);
                if (!isCanceled) {
                    ToastUtil.showToastCenter(PaintBrushActivity.this, "保存成功!\n保存路径为：" + mCompileVideoPath);
                }
                MediaScannerUtil.scanFile(mCompileVideoPath, "video/mp4");
                if (m_streamingContext != null && m_timeline != null) {
                    m_streamingContext.connectTimelineWithLiveWindow(m_timeline, m_liveWindow);
                    playVideo(0, m_timeline.getDuration());
                } else {
                    finish();
                }
            }

            @Override
            public void onCompileFailed(NvsTimeline nvsTimeline) {
                mCompilePage.setVisibility(View.GONE);
                Util.showDialog(PaintBrushActivity.this, "生成失败", "请检查手机存储空间");
            }
        });
        // 取消生成视频
        mCompileCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCanceled = true;
                m_streamingContext.stop();
                mCompilePage.setVisibility(View.GONE);
                playVideo(0, m_timeline.getDuration());
            }
        });

        // 设置面板滑动监听
        m_operateLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 向下滑动
                        if (mCurPosY - mPosY > 0 && (Math.abs(mCurPosY - mPosY) > 15)) {
                            closeEditBrush();
                        }
                        break;
                }
                return true;
            }

        });
    }

    private boolean checkIfEdit() {
        if (m_fxList != null && !m_fxList.isEmpty() && m_fxList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    // 生成视频
    private void compileVideo() {
        String compilePath = getVideoStoragePath();
        if (compilePath == null)
            return;
        updateVideoCompileProgress(0);
        long currentMilis = System.currentTimeMillis();
        String videoName = "meicam_" + String.valueOf(currentMilis) + ".mp4";
        compilePath += "/";
        compilePath += videoName;
        mCompileVideoPath = compilePath;
        int compileResolutionGrade = ParameterSettingValues.instance().getCompileResolutionGrade();
        double bitrate = ParameterSettingValues.instance().getCompileBitrate();
        if (bitrate != 0) {
            Hashtable<String, Object> config = new Hashtable<>();
            config.put(NvsStreamingContext.COMPILE_BITRATE, bitrate * 1000000);
            m_streamingContext.setCompileConfigurations(config);
        }
        int encoderFlag = 0;
        if (ParameterSettingValues.instance().disableDeviceEncorder()) {
            encoderFlag = NvsStreamingContext.STREAMING_ENGINE_COMPILE_FLAG_DISABLE_HARDWARE_ENCODER;
        }

        m_streamingContext.compileTimeline(m_timeline, 0, m_timeline.getDuration(), mCompileVideoPath, compileResolutionGrade, NvsStreamingContext.COMPILE_BITRATE_GRADE_HIGH, encoderFlag);
        mCompilePage.setVisibility(View.VISIBLE);
    }

    private void updateVideoCompileProgress(int progress) {
        mCompileProgressBar.setProgress(progress);
        String strProgress = String.valueOf(progress) + "%";
        mCompileProgressVal.setText(strProgress);
    }

    // 视频存储路径
    private String getVideoStoragePath() {
        File compileDir = new File(Environment.getExternalStorageDirectory(), "NvStreamingSdk" + File.separator + "Compile");
        if (!compileDir.exists() && !compileDir.mkdirs()) {
            Log.d(TAG, "Failed to make Compile directory");
            return null;
        }
        return compileDir.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_operate_layout:
                closeEditBrush();
                break;
        }
    }

    /**
     * 显示窗口
     */
    private void showEditBrush() {
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translate.setDuration(300);//动画时间300毫秒
        translate.setFillAfter(false);//动画出 来控件可以点击
        m_operateLayout.setAnimation(translate);
        m_operateLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭窗口
     */
    private void closeEditBrush() {
        if (m_operateLayout.getVisibility() != View.VISIBLE) {
            return;
        }
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        translate.setDuration(200);//动画时间200毫秒
        translate.setFillAfter(false);//动画出来控件可以点击
        m_operateLayout.startAnimation(translate);
        m_operateLayout.setVisibility(View.GONE);
    }

    @Override
    public void onPlaybackPreloadingCompletion(NvsTimeline nvsTimeline) {

    }

    @Override
    public void onPlaybackStopped(NvsTimeline nvsTimeline) {

    }

    @Override
    public void onPlaybackEOF(NvsTimeline nvsTimeline) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                playVideo(0, m_timeline.getDuration());
            }
        });
    }

    @Override
    public void onPlaybackTimelinePosition(NvsTimeline nvsTimeline, long l) {

    }

    @Override
    public void onStreamingEngineStateChanged(int i) {

    }

    @Override
    public void onFirstVideoFramePresented(NvsTimeline nvsTimeline) {

    }

    private void playVideo(long inPoint, long outPoint) {
        if (m_streamingContext.getStreamingEngineState() != NvsStreamingContext.STREAMING_ENGINE_STATE_PLAYBACK) {
            m_streamingContext.playbackTimeline(m_timeline, inPoint, outPoint,
                    NvsStreamingContext.VIDEO_PREVIEW_SIZEMODE_LIVEWINDOW_SIZE, true, 0);
        }
    }

    //  设置recycleView中item的间距
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
        }
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private boolean cancel() {
        if (haveEditTimeline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PaintBrushActivity.this);
            builder.setTitle("提示：");
            String detail = getResources().getString(R.string.paint_brush_out);
            builder.setMessage(detail);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    result = false;
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeAllBrushPaintFx();
                    if (m_streamingContext != null)
                        m_streamingContext.clearCachedResources(false);
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (m_streamingContext != null)
                m_streamingContext.clearCachedResources(false);
            finish();
        }
        return result;
    }

    private void removeAllBrushPaintFx() {
        if (m_timeline == null) {
            return;
        }
        List<NvsTimelineVideoFx> fx_list = new ArrayList<>();
        NvsTimelineVideoFx timelineVideoFx = m_timeline.getFirstTimelineVideoFx();
        while (timelineVideoFx != null) {
            if (timelineVideoFx.getAttachment(PAINT_FX_NAME) != null) {
                fx_list.add(timelineVideoFx);
            }
            timelineVideoFx = m_timeline.getNextTimelineVideoFx(timelineVideoFx);
        }
        if (fx_list.size() > 0) {
            for (NvsTimelineVideoFx fx : fx_list) {
                if (fx == null) {
                    continue;
                }
                m_timeline.removeTimelineVideoFx(fx);
            }
        }
    }

    private boolean haveEditTimeline() {
        if (m_timeline == null) {
            return false;
        }
        NvsTimelineVideoFx timelineVideoFx = m_timeline.getFirstTimelineVideoFx();
        while (timelineVideoFx != null) {
            if (timelineVideoFx.getAttachment(PAINT_FX_NAME) != null) {
                return true;
            }
            timelineVideoFx = m_timeline.getNextTimelineVideoFx(timelineVideoFx);
        }
        return false;
    }


    @Override
    protected void onPause() {
        if (m_streamingContext != null)
            m_streamingContext.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m_streamingContext != null && m_timeline != null) {
            m_streamingContext.connectTimelineWithLiveWindow(m_timeline, m_liveWindow);
            playVideo(0, m_timeline.getDuration());
        } else {
//
        }
    }
}
