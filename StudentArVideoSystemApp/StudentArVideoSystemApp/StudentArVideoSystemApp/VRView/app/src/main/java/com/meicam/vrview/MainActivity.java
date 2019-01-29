package com.meicam.vrview;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.meicam.sdk.NvsAssetPackageManager;
import com.meicam.sdk.NvsAudioResolution;
import com.meicam.sdk.NvsRational;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsTimelineAnimatedSticker;
import com.meicam.sdk.NvsTimelineCaption;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoResolution;
import com.meicam.sdk.NvsVideoTrack;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glGenTextures;


public class MainActivity extends AppCompatActivity implements NvsStreamingContext.PlaybackCallback,
        SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer{

    private String TAG = "VRView";
    private static final float TIMEBASE = 1000000f;

    private NvsStreamingContext m_streamingContext;
    private NvsTimeline m_timeline;
    private NvsVideoTrack m_videoTrack;

    private GLSurfaceView m_GLView;
    private TextView m_playVideo;
    private TextView m_addCaption;
    private TextView m_addSticker;

    private boolean m_hasAddedCaption = false;
    private boolean m_hasAddedSticker = false;

    private StringBuilder m_stickerId;
    private int m_texId;
    private SurfaceTexture m_surfaceTexture;

    private Sphere mSphere;

    private final float TOUCH_SCALE_FACTOR = (float) Math.PI/2000;//角度缩放比例

    private float mPreviousX;//上次的触控位置X坐标
    private float mPreviousY;//上次的触控位置Y坐标

    private float xAngle = 0;
    private float yAngle = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化Streaming Context
        m_streamingContext = NvsStreamingContext.init(this, null);
        setContentView(R.layout.activity_main);

        m_GLView = (GLSurfaceView) findViewById(R.id.GLView);

        m_GLView.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        m_GLView.setRenderer(this);
        m_GLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        m_stickerId = new StringBuilder();
        String packagePath = "assets:/89740AEA-80D6-432A-B6DE-E7F6539C4121.animatedsticker";
        int error = m_streamingContext.getAssetPackageManager().installAssetPackage(packagePath, null, NvsAssetPackageManager.ASSET_PACKAGE_TYPE_ANIMATEDSTICKER, true, m_stickerId);
        if (error != NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_NO_ERROR
                && error != NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_ALREADY_INSTALLED) {
            Log.e(TAG, "Failed to install sticker package!");
        }

        m_playVideo = (TextView) findViewById(R.id.playVideo);
        m_addCaption = (TextView) findViewById(R.id.addCaption);
        m_addSticker = (TextView) findViewById(R.id.addSticker);

        /* 播放 */
        m_playVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = m_streamingContext.getStreamingEngineState();
                if(NvsStreamingContext.STREAMING_ENGINE_STATE_SEEKING == state || NvsStreamingContext.STREAMING_ENGINE_STATE_STOPPED == state) {
                    m_playVideo.setText("停止");
                    m_streamingContext.playbackTimeline(m_timeline, m_streamingContext.getTimelineCurrentPosition(m_timeline), -1, NvsStreamingContext.VIDEO_PREVIEW_SIZEMODE_LIVEWINDOW_SIZE, true, 0);

                } else if(NvsStreamingContext.STREAMING_ENGINE_STATE_PLAYBACK == state) {
                    m_playVideo.setText("播放");
                    m_streamingContext.stop();
                }
            }
        });

        /* 添加字幕 */
        m_addCaption.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!m_hasAddedCaption) {
                    inputCaption();
                } else {
                    NvsTimelineCaption caption = m_timeline.getFirstCaption();
                    while (caption!=null) {
                        caption = m_timeline.removeCaption(caption);
                    }
                    m_addCaption.setText("添加字幕");
                    m_hasAddedCaption = false;
                }
                m_playVideo.setText("播放");
                seekTimeline();
            }
        });

                /* 添加贴纸 */
        m_addSticker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!m_hasAddedSticker) {
                    if (m_stickerId.length() == 0) {
                        return;
                    }
                    NvsTimelineAnimatedSticker sticker = m_timeline.addPanoramicAnimatedSticker(0, m_timeline.getDuration(), m_stickerId.toString());
                    sticker.setPolarAngleRange(45);
                    sticker.setCenterPolarAngle(120);
                    sticker.setCenterAzimuthAngle(90);
                    m_addSticker.setText("删除贴纸");

                    m_hasAddedSticker = true;
                }else {
                    NvsTimelineAnimatedSticker sticker = m_timeline.getFirstAnimatedSticker();
                    while (sticker!=null) {
                        sticker = m_timeline.removeAnimatedSticker(sticker);
                    }
                    m_addSticker.setText("添加贴纸");
                    m_hasAddedSticker = false;
                }
                m_playVideo.setText("播放");
                seekTimeline();
            }
        });
    }


    /*输入字幕*/
    private  void inputCaption() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        /**
         * 实现一弹出对话框，就弹出键盘
         */
        userInput.setFocusable(true);
        userInput.setFocusableInTouchMode(true);
        userInput.requestFocus();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager)userInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(userInput, 0);
            }

        }, 200);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                NvsTimelineCaption caption = m_timeline.addPanoramicCaption(userInput.getText()+"", 0, m_timeline.getDuration(), null);
                                caption.setPolarAngleRange(30);
                                caption.setCenterPolarAngle(60);
                                caption.setCenterAzimuthAngle(0);
                                m_addCaption.setText("删除字幕");
                                m_hasAddedCaption = true;
                                seekTimeline();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        final Button btn = ((AlertDialog) alertDialog).getButton(DialogInterface.BUTTON_POSITIVE);
        btn.setEnabled(false);
        userInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                if (name.isEmpty())
                    btn.setEnabled(false);
                else
                    btn.setEnabled(true);
            }
        });
    }

    private void seekTimeline() {
        /* seekTimeline
        * param1: 当前时间线
        * param2: 时间戳 取值范围为  [0, timeLine.getDuration()) (左闭右开区间)
        * param3: 图像预览模式
        * param4: 引擎定位的特殊标志
        * */
        m_streamingContext.seekTimeline(m_timeline, m_streamingContext.getTimelineCurrentPosition(m_timeline), NvsStreamingContext.VIDEO_PREVIEW_SIZEMODE_LIVEWINDOW_SIZE, m_streamingContext.STREAMING_ENGINE_SEEK_FLAG_SHOW_CAPTION_POSTER | m_streamingContext.STREAMING_ENGINE_SEEK_FLAG_SHOW_ANIMATED_STICKER_POSTER);

    }

    private void setupEglContextForConsumer() {
        int[] texs = new int[1];
        glGenTextures(1, texs, 0);
        m_texId = texs[0];

        // 创建 SurfaceTexture
        m_surfaceTexture = new SurfaceTexture(m_texId);
        m_surfaceTexture.setOnFrameAvailableListener(this);

    }

    //创建timeline
    private void initTimeline()
    {
        if (null == m_streamingContext) {
            Log.e(TAG, "m_streamingContext is null!");
            return;
        }

        NvsVideoResolution videoEditRes = new NvsVideoResolution();

        //宽高比例2:1
        videoEditRes.imageWidth = 1920;
        videoEditRes.imageHeight = 960;
        videoEditRes.imagePAR = new NvsRational(1, 1);
        NvsRational videoFps = new NvsRational(25, 1);

        NvsAudioResolution audioEditRes = new NvsAudioResolution();
        audioEditRes.sampleRate = 44100;
        audioEditRes.channelCount = 2;

        m_timeline = m_streamingContext.createTimeline(videoEditRes, videoFps, audioEditRes);
        if (null == m_timeline) {
            Log.e(TAG, "m_timeline is null!");
            return;
        }

        //将timeline与surfacetexture绑定
        m_streamingContext.connectTimelineWithSurfaceTexture(m_timeline,m_surfaceTexture);
        m_streamingContext.setPlaybackCallback(this);

        m_videoTrack = m_timeline.appendVideoTrack();
        if (null == m_videoTrack) {
            Log.e(TAG, "m_videoTrack is null!");
            return;
        }

        //添加视频
        String video1Path = "assets:/video1.mp4";
        String video2Path = "assets:/video2.mp4";
        NvsVideoClip videoClip = m_videoTrack.appendClip(video1Path);
        videoClip.appendBuiltinFx("Pinky");//添加内建视频特效
        videoClip = m_videoTrack.appendClip(video2Path);
        videoClip.appendBuiltinFx("Sweet");//添加内建视频特效

        seekTimeline();
        //m_streamingContext.playbackTimeline(m_timeline,0,m_timeline.getDuration(),NvsStreamingContext.VIDEO_PREVIEW_SIZEMODE_LIVEWINDOW_SIZE, true, 0);
    }

    @Override
    public void onPlaybackPreloadingCompletion(NvsTimeline var1)
    {

    }

    @Override
    public void onPlaybackStopped(NvsTimeline var1)
    {
        Log.d("Meishe", "play stopped!");

    }

    @Override
    public void onPlaybackEOF(NvsTimeline var1)
    {
        Log.d("Meishe", "play EOF!");
        m_streamingContext.playbackTimeline(m_timeline,0,m_timeline.getDuration(),NvsStreamingContext.VIDEO_PREVIEW_SIZEMODE_LIVEWINDOW_SIZE, true, 0);
    }

    //刷新texture image
    private void receiveImageForConsumer()
    {
        Log.d("Meishe", "SurfaceTexture frame available!");

        m_surfaceTexture.updateTexImage();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //在glthread中调用，surfacetexture的创建与刷新都在glthread线程中
        m_GLView.queueEvent(
                new Runnable() {
                    @Override
                    public void run()
                    {
                        receiveImageForConsumer();
                    }
                });
//        m_GLView.requestRender();
    }


    @Override
    protected void onResume() {
        super.onResume();
        m_GLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_streamingContext.stop();
        m_GLView.onPause();
    }

    @Override
    protected void onDestroy() {
        m_streamingContext = null;
        NvsStreamingContext.close();
        super.onDestroy();
    }

    //Renderer回调函数，surface初始化时调用
    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        //初始化surfacetexture
        setupEglContextForConsumer();

        //绑定surfacetexture
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES , m_texId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //创建球体，并传入纹理id
        mSphere=new Sphere(this.getApplicationContext(),m_texId);
        mSphere.create();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);


        //在UI线程中创建timeline，SDK中绝大部分函数都应在UI线程调用
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                initTimeline();
            }
        });

    }

    //Renderer回调函数，surface变化时调用
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSphere.setSize(width, height);
        GLES20.glViewport(0,0,width,height);
    }

    //Renderer回调函数，用于绘制surface
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1,1,1,1);

        //进行球面纹理绘制
        mSphere.draw();
    }


    //触摸事件回调方法
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        if(!isTouchPointInView(m_GLView,(int)x,(int)y))
            return false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;//计算触控点X位移
                float dy = y - mPreviousY;//计算触控点Y位移
                float absX = Math.abs(dx);
                float absY = Math.abs(dy);
                if(absX > absY) {
                    float xangle = dx * TOUCH_SCALE_FACTOR;
                    xAngle += xangle;//设置方位角

                } else {
                    float yangle = dy * TOUCH_SCALE_FACTOR;
                    yAngle += yangle;//设置天顶角
                }
                setCenterPoint();
        }
        //记录触控点位置
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    private void setCenterPoint(){

        if (xAngle >= 2 * Math.PI || xAngle <= -2 * Math.PI)
            xAngle = 0;

        if (yAngle >= 2 * Math.PI || yAngle <= -2 * Math.PI)
            yAngle = 0;

        float centerX = (float) (Math.cos(yAngle) * Math.cos(xAngle));
        float centerY = (float) (Math.cos(yAngle) * Math.sin(xAngle));
        float centerZ = (float) Math.sin(yAngle);

        if(mSphere == null){
            return;
        }

        if(yAngle < -Math.PI*3/2 || yAngle > Math.PI*3/2 || (yAngle > -Math.PI/2 && yAngle < Math.PI/2)){
            mSphere.setCenterPointExt(centerX, centerY, centerZ, 1f);
        }else if((yAngle >= -Math.PI*3/2 &&  yAngle <= -Math.PI/2) || (yAngle >= Math.PI/2 && yAngle <= Math.PI*3/2)){
            mSphere.setCenterPointExt(centerX, centerY, centerZ, -1f);
        }
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if(y>=top && y<=bottom && x>=left && x <= right)
            return true;
        return false;
    }

}
