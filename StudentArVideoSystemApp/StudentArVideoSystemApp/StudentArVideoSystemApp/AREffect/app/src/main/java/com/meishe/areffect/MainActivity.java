package com.meishe.areffect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NvsStreamingContext.CaptureDeviceCallback, SetUpCtl.SetUpCtlListener{

    private static final String TAG = "ARFace";
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 2;

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


    private ArrayList m_lstCaptureScene;
    private SetUpCtl m_setUpCtl;
    private TextView m_shootingTimeTxt;
    private int m_shootingTime = 0;
    private Timer m_shootingTimer;
    private double m_whiteningDefaultValue = 0;
    private double m_strengthDefaultValue = 0;
    private double m_reddeningDefaultValue = 0;
    private double m_daYanDefaultValue = 0;
    private double m_shouLianDefaultValue = 0;
    private String m_recordVideoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!this.isTaskRoot()) { //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent=getIntent();
            String action=mainIntent.getAction();
            if(mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                Log.e("===>", "re add");
                return;//finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //初始化Streaming Context
        m_streamingContext = NvsStreamingContext.init(this, null);
        if (m_streamingContext == null)
            return;

        setContentView(R.layout.activity_main);
        m_setUpCtl = (SetUpCtl) findViewById(R.id.setUpCtl);
        m_shootingTimeTxt = (TextView) findViewById(R.id.shootingTimeTxt);

        // 初始化AR Effect，全局只需一次
        NvsAREffect.Init("assets:/model/facedetectmodel.xml", "assets:/model/clnf.mod");

        m_liveWindow = (NvsLiveWindow) findViewById(R.id.liveWindow);
        m_switchBtn = (Button) findViewById(R.id.switchBtn);


        //
        // 采集特效列表
        m_lstCaptureFx = new ArrayList();
        m_lstCaptureFx.add("None");
        m_lstCaptureFx.addAll(m_streamingContext.getAllBuiltinCaptureVideoFxNames());
        m_setUpCtl.setCaptureFxData(m_lstCaptureFx);

        //AREffect scene
        m_lstCaptureScene = new ArrayList();
        m_lstCaptureScene.add("None");
        installCaptureScene(m_lstCaptureScene);
        m_setUpCtl.setCaptureSceneData(m_lstCaptureScene);


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
                        m_permissionGranted = true;
                        if (!startCapturePreview(false))
                            return;
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
        }

        // 开启美颜
        m_beauty = m_streamingContext.insertBeautyCaptureVideoFx(0);

        // 开启人脸特效
        m_ARFace = m_streamingContext.appendBuiltinCaptureVideoFx("AR Effect");

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

        // 获取默认值
        m_whiteningDefaultValue = m_beauty.getFloatVal("Whitening");
        m_reddeningDefaultValue = m_beauty.getFloatVal("Reddening");
        m_strengthDefaultValue = m_beauty.getFloatVal("Strength");
        m_daYanDefaultValue = 0;
        m_shouLianDefaultValue = 0;

        // 设置控件监听器
        m_setUpCtl.setListener(this);
        m_setUpCtl.resetData();
    }

    @Override
    protected void onDestroy() {
        m_streamingContext.removeAllCaptureVideoFx();
        m_streamingContext.stop();
        super.onDestroy();
        Log.e("===>", "onDestroy");
    }

    @Override
    protected void onPause() {
        stopTimer();
        super.onPause();
        Log.e("===>", "onPause");
    }

    @Override
    protected void onResume() {
        startCapturePreview(false);
        super.onResume();
        Log.e("===>", "onResume");
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
                        m_permissionGranted = true;
                        startCapturePreview(false);
                    } else
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    m_permissionGranted = true;
                    startCapturePreview(false);
                } else
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_CODE:
                m_permissionGranted = true;
                startCapturePreview(false);
                break;
        }
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
            int QR = NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_HIGH;
            if (m_currentDeviceIndex == 0)
                QR = NvsStreamingContext.VIDEO_CAPTURE_RESOLUTION_GRADE_MEDIUM;
            if (!m_streamingContext.startCapturePreview(m_currentDeviceIndex, QR,
                    NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_CAPTURE_BUDDY_HOST_VIDEO_FRAME
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_STRICT_PREVIEW_VIDEO_SIZE
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_DONT_USE_SYSTEM_RECORDER
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_LOW_PIPELINE_SIZE, null)) {
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

    private String installSingleCaptureScene(String path)
    {
        StringBuilder themeId = new StringBuilder();
        int error = m_streamingContext.getAssetPackageManager().installAssetPackage(path, null,
                NvsAssetPackageManager.ASSET_PACKAGE_TYPE_CAPTURESCENE, true, themeId);
        if (error != NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_NO_ERROR &&
                error != NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_ALREADY_INSTALLED) {
            Log.d(TAG, "Failed to install package!"+path);
            return null;
        }

        if(error == NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_ALREADY_INSTALLED){
            error = m_streamingContext.getAssetPackageManager().upgradeAssetPackage(path, null,
                    NvsAssetPackageManager.ASSET_PACKAGE_TYPE_CAPTURESCENE, true, themeId);
            if (error != NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_NO_ERROR) {
                Log.d(TAG, "Failed to upgrade package!"+path);
            }
        }

        return themeId.toString();
    }

    private void installCaptureScene(ArrayList list) {
        String[] path_list = null;
        try {
            path_list = getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String Extension = "capturescene";

        if(path_list != null){

            for(int i = 0; i < path_list.length; ++i){
                String filepath = path_list[i];
                if(filepath == null)
                    continue;
                if(filepath.length() <= Extension.length())
                    continue;
                if(!filepath.substring(filepath.length() - Extension.length()).equals(Extension))
                    continue;
                String themeId = installSingleCaptureScene("assets:/" + path_list[i]);
                if(themeId != null)
                    list.add(themeId);
            }
        }

        //sd card path
         String sdPath = Environment.getExternalStorageDirectory().getPath() + "/areffect";
        File file = new File(sdPath);
        if(file.exists()) {
            File[] files = new File(sdPath).listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile()) {
                        if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                        {
                            String themeId = installSingleCaptureScene(f.getPath());
                            if (themeId != null)
                                list.add(themeId);
                        }
                    }

                }
            }
        }
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

    public void onLiveWindowClicked(View view){
        m_setUpCtl.hideContent();
    }


    // 道具
    @Override
    public void onDaojuItemSelected(String effectItemName) {
        if (effectItemName == null)
            return;
        if (effectItemName.equals("None")) {
            m_streamingContext.removeCurrentCaptureScene();
            return;
        }

        m_streamingContext.applyCaptureScene(effectItemName);
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
        if (m_beauty == null)
            return;
        float val = ((float)progress)/((float)max);
        m_beauty.setFloatVal("Strength", val);
        Log.d(TAG, "onBlurSelected: " + val);
    }

    // 美白
    @Override
    public void onWhiteSelected(int progress, int max){
        if (m_beauty == null)
            return;
        float val = ((float)progress)/((float)max);
        m_beauty.setFloatVal("Whitening", val);
        Log.d(TAG, "onWhiteSelected: " + val);
    }

    // 红润
    @Override
    public void onRuddySelected(int progress, int max) {
        if (m_beauty == null)
            return;
        float val = ((float)progress)/((float)max);
        m_beauty.setFloatVal("Reddening", val);
        Log.d(TAG, "onRuddySelected: " + val);
    }

    // 美型强度
    @Override
    public void onBeautyShapeValue(String beauty_shape_type, String beauty_shape_value, int progress, int max){
        if (m_ARFace == null || beauty_shape_type == null || beauty_shape_value == null) {
            return;
        }
        // 窄脸和瘦鼻值是相反的
        if(beauty_shape_value.equals("Face Width Warp Ratio") || beauty_shape_value.equals("Nose Width Warp Ratio")) {
            float val = (float)(100 - progress) / 100;
            m_ARFace.setFloatVal(beauty_shape_value, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        } else if(beauty_shape_value.equals("Face Length Warp Ratio")) { // 小脸只有[-1,0]的值
            float val = (float)(200 - progress) / 200 - 1.0f;
            m_ARFace.setFloatVal(beauty_shape_value, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        } else {
            float val = (float)(progress - 100) / 100;
            m_ARFace.setFloatVal(beauty_shape_value, val);
            Log.d(TAG, "onBeautyShapeValue: " + val);
        }
    }

    // 选择一个美型类型
    @Override
    public void onBeautyShapeSelected(String beauty_shape_type, String beauty_shape_value){
        if (m_ARFace == null || beauty_shape_type == null || beauty_shape_value == null) {
            return;
        }
        m_ARFace.setBooleanVal(beauty_shape_type, true);
        float value = (float) m_ARFace.getFloatVal(beauty_shape_value);
        if(beauty_shape_value.equals("Face Length Warp Ratio")) { // 小脸只有[-1,0]的值
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

            m_switchBtn.setClickable(true);
            Toast.makeText(this, "录制完成，视频保存在" + m_recordVideoPath, Toast.LENGTH_SHORT).show();
            return;
        }

        File ARfaceDir = new File(Environment.getExternalStorageDirectory(), "NvStreamingSdk" + File.separator + "ARface");
        if (!ARfaceDir.exists() && !ARfaceDir.mkdirs()) {

            Toast.makeText(this, "Failed to make Record directory", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to make Record directory");

            return;
        }

        m_setUpCtl.setRecordImageViewSelected();
        String fileName = getCharacterAndNumber() + ".mp4";

        File file = new File(ARfaceDir, fileName);
        if (file.exists())
            file.delete();

        m_recordVideoPath = file.getAbsolutePath();
        if (!m_streamingContext.startRecording(m_recordVideoPath))
            return;

        m_switchBtn.setClickable(false);
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

