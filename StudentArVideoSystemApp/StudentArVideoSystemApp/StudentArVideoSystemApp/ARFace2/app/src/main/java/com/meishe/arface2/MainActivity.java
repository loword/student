package com.meishe.arface2;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meicam.sdk.*;
import com.meishe.arface2.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NvsStreamingContext.CaptureDeviceCallback, SetUpCtl.SetUpCtlListener{

    private static final String TAG = "ARFace2";
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 2;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 3;

    private NvsLiveWindow m_liveWindow;
    private Button m_switchBtn;
    private boolean m_supportAutoFocus = false;
    private int m_currentDeviceIndex;
    private NvsStreamingContext m_streamingContext;
    private boolean m_permissionGranted;
    private ArrayList m_lstCaptureFx;
    private NvsCaptureVideoFx m_filter = null;
    private NvsCaptureVideoFx m_beauty = null;
    private NvsCaptureVideoFx m_ARFace = null;
    private SetUpCtl m_setUpCtl;
    private TextView m_shootingTimeTxt;
    private Button m_memoryInfoBtn;
    private TextView m_memoryInfoTxt;
    private int m_shootingTime = 0;
    private Timer m_shootingTimer;
    private double m_whiteningDefaultValue = 0;
    private double m_strengthDefaultValue = 0;
    private double m_reddeningDefaultValue = 0;
    private double m_daYanDefaultValue = 0;
    private double m_shouLianDefaultValue = 0;
    private double m_chengDuDefaultValue = 0;


    private Handler m_timerhandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            UpdateMemoryInfoText();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //初始化Streaming Context
        m_streamingContext = NvsStreamingContext.init(this, null);
        if (m_streamingContext == null)
            return;

        setContentView(R.layout.activity_main);
        m_setUpCtl = (SetUpCtl) findViewById(R.id.setUpCtl);
        m_shootingTimeTxt = (TextView) findViewById(R.id.shootingTimeTxt);
        m_memoryInfoBtn = (Button) findViewById(R.id.memoryInfoBtn);
        m_memoryInfoTxt = (TextView) findViewById(R.id.memoryInfo);
        m_memoryInfoTxt.setVisibility(View.GONE);

        // 初始化AR Face，全局只需一次
        NvsFaceEffect2Init.authentification(this, "assets:/NvFace2.lic");
        NvsFaceEffect2Init.setupModeData("assets:/NvFace2Data.model");

        m_liveWindow = (NvsLiveWindow) findViewById(R.id.liveWindow);
        m_switchBtn = (Button) findViewById(R.id.switchBtn);


        // 采集特效列表
        m_lstCaptureFx = new ArrayList();
        m_lstCaptureFx.add("None");
        m_lstCaptureFx.addAll(m_streamingContext.getAllBuiltinCaptureVideoFxNames());
        m_setUpCtl.setCaptureFxData(m_lstCaptureFx);



        //给Streaming Context设置采集设备回调接口
        m_streamingContext.setCaptureDeviceCallback(this);
        //采集设备数量判定
        if (m_streamingContext.getCaptureDeviceCount() > 1) {
            m_switchBtn.setEnabled(true);
            m_currentDeviceIndex = 1;
        } else {
            m_switchBtn.setEnabled(false);
            m_currentDeviceIndex = 0;
        }
        // 将采集预览输出连接到NvsLiveWindow控件
        if (!m_streamingContext.connectCapturePreviewWithLiveWindow(m_liveWindow)) {
            Log.e(TAG, "Failed to connect capture preview with livewindow!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                    if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                            m_permissionGranted = true;
                            if (!startCapturePreview(false))
                                return;
                            updateStickerDataFromLocal();
                        }else{
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
                        }
                    }else{
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                    }
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
            }
            else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            }
        } else {
            m_permissionGranted = true;
            if (!startCapturePreview(false))
                return;

            updateStickerDataFromLocal();
        }


        // 开启脸部特效
        m_ARFace = m_streamingContext.appendBuiltinCaptureVideoFx("Face Effect2");     //添加美颜采集特效
        m_ARFace.setFloatVal("Strength", 0.3f);//设置美颜强度值
        m_ARFace.setFloatVal("Whitening", 0.3f);
        m_ARFace.setFloatVal("Reddening", 0.3f);
        m_ARFace.setFloatVal("Eye Enlarging", 0);
        m_ARFace.setStringVal("Sticker Mode", "assets:/sticker/dedormationccq.zip");

        m_switchBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (m_currentDeviceIndex == 1)
                    m_currentDeviceIndex = 0;
                else
                    m_currentDeviceIndex = 1;
                startCapturePreview(true);
            }
        });

        m_memoryInfoBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(m_memoryInfoTxt.getVisibility() == View.GONE)
                    m_memoryInfoTxt.setVisibility(View.VISIBLE);
                else
                    m_memoryInfoTxt.setVisibility(View.GONE);
            }
        });

        // 获取默认值
        m_whiteningDefaultValue = m_ARFace.getFloatVal("Whitening");
        m_reddeningDefaultValue = m_ARFace.getFloatVal("Reddening");
        m_strengthDefaultValue = m_ARFace.getFloatVal("Strength");
        m_chengDuDefaultValue = m_ARFace.getFloatVal("Face Shape Level");
        m_daYanDefaultValue = m_ARFace.getFloatVal("Eye Enlarging");
        m_shouLianDefaultValue = m_ARFace.getFloatVal("Cheek Thinning");

        // 设置控件监听器
        m_setUpCtl.setListener(this);
        m_setUpCtl.resetData();

        m_timerhandler.postDelayed(timerRunnable, 500);
    }

    public void UpdateMemoryInfoText()
    {
        String memoryInfoText = getAppMemoryInfo();
        m_memoryInfoTxt.setText(memoryInfoText);
        m_timerhandler.postDelayed(timerRunnable, 500);
    }

    private void updateStickerDataFromLocal()
    {
        ArrayList<String> fullPath_List = new ArrayList<String>();
        fullPath_List.add("none");
        fullPath_List.add("assets:/sticker/diss.zip");
        fullPath_List.add("assets:/sticker/hdj.zip");
        fullPath_List.add("assets:/sticker/glassFour.zip");

        ArrayList<String> loaclPath_List = new ArrayList<String>();
        String sdPath = Environment.getExternalStorageDirectory().getPath() + "/sticker";
        File file = new File(sdPath);
        if(file.exists()) {
            loaclPath_List = GetAllFilesFromSD(sdPath, "zip", true);
        }

        fullPath_List.addAll(loaclPath_List);
        m_setUpCtl.setStickerata(fullPath_List);
    }


    @Override
    protected void onDestroy() {
        //streamingContext销毁
        NvsFaceEffect2Init.finish();
        m_streamingContext = null;
        NvsStreamingContext.close();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_streamingContext.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            return;

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            m_permissionGranted = true;
                            startCapturePreview(false);
                            updateStickerDataFromLocal();
                        } else
                            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
                    } else
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        m_permissionGranted = true;
                        startCapturePreview(false);
                        updateStickerDataFromLocal();
                    } else
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
                } else
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    m_permissionGranted = true;
                    startCapturePreview(false);
                    updateStickerDataFromLocal();
                } else
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
                break;
            case REQUEST_READ_EXTERNAL_STORAGE_CODE:
                m_permissionGranted = true;
                startCapturePreview(false);
                updateStickerDataFromLocal();
                break;
        }
    }


    public static ArrayList<String> GetAllFilesFromSD(String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
    {
        ArrayList<String> path_list = new ArrayList<String>();

        File[] files = new File(Path).listFiles();
        if(files == null)
            return  null;

        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                    path_list.add(f.getPath());

                if (!IsIterative)
                    break;
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)  //忽略点文件（隐藏文件/文件夹）
            {
                ArrayList<String> sub_path_list = GetAllFilesFromSD(f.getPath(), Extension, IsIterative);

                path_list.addAll(sub_path_list);
            }
        }

        return path_list;
    }

    private void updateSettingsWithCapability(int deviceIndex) {
        //获取采集设备能力描述对象，设置自动聚焦，曝光补偿，缩放
        NvsStreamingContext.CaptureDeviceCapability capability = m_streamingContext.getCaptureDeviceCapability(deviceIndex);
        if (null == capability)
            return;
        //是否支持自动聚焦
        if (capability.supportAutoFocus) {
            m_supportAutoFocus = true;
        }
    }
    // 获取当前引擎状态
    private int getCurrentEngineState() {
        return m_streamingContext.getStreamingEngineState();
    }
    private boolean startCapturePreview(boolean deviceChanged) {
        // 判断当前引擎状态是否为采集预览状态
        if (m_permissionGranted && (deviceChanged || getCurrentEngineState() != NvsStreamingContext.STREAMING_ENGINE_STATE_CAPTUREPREVIEW)) {
            int QR = NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_MEDIUM;
            if (m_currentDeviceIndex == 0)
                QR = NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_MEDIUM;
            if (!m_streamingContext.startCapturePreview(m_currentDeviceIndex, QR,
                    NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_CAPTURE_BUDDY_HOST_VIDEO_FRAME
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_STRICT_PREVIEW_VIDEO_SIZE
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_DONT_USE_SYSTEM_RECORDER, null)) {
                Log.e(TAG, "Failed to start capture preview!");
                return false;
            }
        }

        if (m_supportAutoFocus) {
            Rect rectFrame = new Rect();
            rectFrame.set(m_liveWindow.getWidth()/2-10, m_liveWindow.getHeight()/2-10, m_liveWindow.getWidth()/2+10, m_liveWindow.getHeight()/2+10);
            m_streamingContext.startAutoFocus(new RectF(rectFrame));//启动自动聚焦
        }
        return true;
    }


    private String getAppMemoryInfo()
    {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);

        String InfoTxt = new String();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Map<String, String>  info = memoryInfo.getMemoryStats();
            if(info != null){
                Set<String> keys = info.keySet();
                for(String keyTxt : keys){
                    String value = info.get(keyTxt);
                    InfoTxt +=keyTxt + "  :  " + value + "\n";
                }
            }
        }

        int total = memoryInfo.getTotalPss();

        InfoTxt += "total :  "+ total + "(kb) \n";

        return InfoTxt;
    }

    @Override
    public void onCaptureDeviceCapsReady(int captureDeviceIndex)
    {
        if (captureDeviceIndex != m_currentDeviceIndex)
            return;
        updateSettingsWithCapability(captureDeviceIndex);
    }
    @Override
    public void onCaptureDevicePreviewResolutionReady(int captureDeviceIndex)
    {
    }

    @Override
    public void onCaptureDevicePreviewStarted(int captureDeviceIndex)
    {
    }

    @Override
    public void onCaptureDeviceError(int captureDeviceIndex, int errorCode)
    {
    }

    @Override
    public void onCaptureDeviceStopped(int captureDeviceIndex)
    {
    }

    @Override
    public void onCaptureDeviceAutoFocusComplete(int captureDeviceIndex, boolean succeeded)
    {
    }

    @Override
    public void onCaptureRecordingFinished(int captureDeviceIndex)
    {
    }

    @Override
    public void onCaptureRecordingError(int captureDeviceIndex)
    {
    }

    @Override
    protected void onPause() {
        stopTimer();

        //停止引擎
        m_streamingContext.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startCapturePreview(false);
        super.onResume();
    }

    public void onLiveWindowClicked(View view){
        m_setUpCtl.hideContent();
    }


    // 道具
    @Override
    public void onDaojuItemSelected(String effectItemName) {
        if (m_ARFace == null)
            return;
        if (effectItemName.equals("none"))
            m_ARFace.setStringVal("Sticker Mode", "");
        else
            m_ARFace.setStringVal("Sticker Mode", effectItemName);
        Log.d(TAG, "onDaojuItemSelected: " + effectItemName);
    }

    // 滤镜
    @Override
    public void onFilterSelected(String filterName){
        if (m_filter != null) {
            m_streamingContext.removeCaptureVideoFx(1); //移除滤镜特效
            m_filter = null;
        }
        if (filterName.equals("None")) {
            m_filter = null;
            return;
        }
        //添加内建采集特效
        m_filter = m_streamingContext.insertBuiltinCaptureVideoFx(filterName, 1);
        Log.d(TAG, "onFilterSelected: " + filterName);
    }

    // 磨皮
    @Override
    public void onBlurSelected(int progress, int max){
        if (m_ARFace == null)
            return;
        float val = ((float)progress)/((float)max);
        m_ARFace.setFloatVal("Strength", val);
        Log.d(TAG, "onBlurSelected: " + val);
    }

    // 美白
    @Override
    public void onWhiteSelected(int progress, int max){
        if (m_ARFace == null)
            return;
        float val = ((float)progress)/((float)max);
        m_ARFace.setFloatVal("Whitening", val);
        Log.d(TAG, "onWhiteSelected: " + val);
    }

    // 红润
    @Override
    public void onRuddySelected(int progress, int max) {
        if (m_ARFace == null)
            return;
        float val = ((float)progress)/((float)max);
        m_ARFace.setFloatVal("Reddening", val);
        Log.d(TAG, "onRuddySelected: " + val);
    }

    // 美型强度
    @Override
    public void onBeautyShapeValue(String beauty_shape_type, int progress, int max){
        if (m_ARFace == null || beauty_shape_type == null) {
            return;
        }
        // 窄脸和瘦鼻值是相反的
        if(beauty_shape_type.equals("Face Width Warp Ratio") || beauty_shape_type.equals("Nose Width Warp Ratio")) {
            float val = (float)(100 - progress) / 100;
            m_ARFace.setFloatVal(beauty_shape_type, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        } else if(beauty_shape_type.equals("Face Length Warp Ratio")) { // 小脸只有[-1,0]的值
            float val = (float)(200 - progress) / 200 - 1.0f;
            m_ARFace.setFloatVal(beauty_shape_type, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        } else {
            float val = (float)(progress - 100) / 100;
            m_ARFace.setFloatVal(beauty_shape_type, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        }
    }

    // 选择一个美型类型
    @Override
    public void onBeautyShapeSelected(String beauty_shape_type){
        if (m_ARFace == null || beauty_shape_type == null) {
            return;
        }
        float value = (float) m_ARFace.getFloatVal(beauty_shape_type);
        if(beauty_shape_type.equals("Face Length Warp Ratio")) { // 小脸只有[-1,0]的值
            m_setUpCtl.setBeautyShapeProgress((int) (-value * 200), -value * 100);
        } else {
            m_setUpCtl.setBeautyShapeProgress((int) (value * 100 + 100), value * 100);
        }
    }

    @Override
    public void onRecordBtnClicked(){


        // 当前在录制状态，可停止视频录制
        if (getCurrentEngineState() == m_streamingContext.STREAMING_ENGINE_STATE_CAPTURERECORDING) {
            m_streamingContext.stopRecording();
            stopTimer();
            return;
        }

        File ARfaceDir = new File(Environment.getExternalStorageDirectory(), "NvStreamingSdk" + File.separator + "ARface");
        if (!ARfaceDir.exists() && !ARfaceDir.mkdirs()) {

            Toast.makeText(this, "Failed to make Record directory", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to make Record directory");

            return;
        }

        m_setUpCtl.setRecordImageViewSelected();
        String fileName = getCharacterAndNumber() + ".mp4";

        File file = new File(ARfaceDir, fileName);
        if (file.exists())
            file.delete();

        if (!m_streamingContext.startRecording(file.getAbsolutePath()))
            return;

        startTimer();
    }

    @Override
    public void onBeautyColorReset() {
        m_setUpCtl.setBlurProgress((int) (m_strengthDefaultValue * 100));
        m_setUpCtl.setWhiteProgress((int) (m_whiteningDefaultValue * 100));
        m_setUpCtl.setRuddyProgress((int) (m_reddeningDefaultValue * 100));
    }

    private void stopTimer(){
        if(m_shootingTimer != null){
            m_shootingTimer.cancel();
            m_setUpCtl.setRecordImageViewSelected();
            m_shootingTimer = null;
        }
    }

    private void startTimer(){
        m_shootingTime = 0;
        m_shootingTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_shootingTimeTxt.setText(formatTimeStrWithUs(m_shootingTime));
                        m_shootingTime += 1000000;
                    }
                });

            }
        };
        m_shootingTimer.schedule(timerTask, 0, 1000);
    }

    private String getCharacterAndNumber() {
        String rel = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        rel = formatter.format(curDate);
        return rel;
    }

    /*格式化时间(us)*/
    private String formatTimeStrWithUs(int us) {
        int second = us / 1000000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String timeStr;
        if (us == 0) {
            timeStr = "00:00";
        }
        if (hh > 0) {
            timeStr = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            timeStr = String.format("%02d:%02d", mm, ss);
        }
        return timeStr;

    }
}

