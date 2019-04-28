package com.meishe.sdkdemo.superzoom;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.meicam.effect.sdk.NvsEffectSdkContext;
import com.meicam.sdk.NvsRational;
import com.meicam.sdk.NvsVideoResolution;
import com.meishe.effect.NvSuperZoom;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BasePermissionActivity;
import com.meishe.sdkdemo.boomrang.view.RoundProgressView;
import com.meishe.sdkdemo.interfaces.TipsButtonClickListener;
import com.meishe.sdkdemo.superzoom.fxview.CenterHorizontalView;
import com.meishe.sdkdemo.superzoom.fxview.CenterHorizontalViewAdapter;
import com.meishe.sdkdemo.superzoom.helper.EGLHelper;
import com.meishe.sdkdemo.superzoom.helper.ShaderHelper;
import com.meishe.sdkdemo.superzoom.processor.CameraProxy;
import com.meishe.sdkdemo.superzoom.processor.MediaEncoder;
import com.meishe.sdkdemo.superzoom.processor.MediaMuxerWrapper;
import com.meishe.sdkdemo.superzoom.processor.MediaVideoEncoder;
import com.meishe.sdkdemo.superzoom.zoomutils.Accelerometer;
import com.meishe.sdkdemo.superzoom.zoomutils.RawResourceReader;
import com.meishe.sdkdemo.superzoom.zoomutils.STMobileDetected;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.Logger;
import com.meishe.sdkdemo.utils.PathUtils;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.asset.NvAssetManager;
import com.sensetime.stmobile.model.STHumanAction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glGenTextures;
import static com.meishe.sdkdemo.superzoom.processor.MediaMuxerWrapper.MSG_FINISH_RECORDING;
import static com.meishe.sdkdemo.utils.Constants.ASSET_DOWNLOAD_INPROGRESS;
import static com.meishe.sdkdemo.utils.Constants.ASSET_LIST_REQUEST_SUCCESS;


public class SuperZoomActivity extends BasePermissionActivity implements SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer {

    private static final String TAG = "SuperZoomActivity";
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 2;
    public final static String POSITION_COORDINATE = "position";
    public final static String TEXTURE_UNIFORM = "inputImageTexture";
    public final static String TEXTURE_COORDINATE = "inputTextureCoordinate";
    private static float squareSize = 1.0f;
    private static float m_squareCoords[] = {
            -squareSize, squareSize,   // top left
            -squareSize, -squareSize,   // bottom left
            squareSize, -squareSize,    // bottom right
            squareSize, squareSize}; // top right
    private static short m_drawOrder[] = {0, 1, 2, 0, 2, 3};
    private float textureCoords[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f};

    private RoundProgressView m_buttonRecord;
    private ImageView m_buttonSwitchFacing;
    private AppCompatButton m_closeBtn;
    private GLSurfaceView m_GLView;
    private CenterHorizontalView mSuperZoomFxView;
    private LinearLayout mToolListLl;
    private ImageView m_buttonFlash;

    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;    // 默认前置摄像头
    private Accelerometer mAccelerometer = null;
    private CameraProxy m_cameraProxy;
    private boolean m_permissionGranted;
    private boolean m_isPreviewing = false;
    private int m_oritation;
    private int m_width, m_height;
    private int m_shaderProgram;
    private FloatBuffer m_vertexBuffer;
    private ShortBuffer m_drawListBuffer;
    private float[] m_videoTextureTransform = new float[16];
    private SurfaceTexture m_cameraPreviewTexture;
    private HandlerThread mSurfaceAvailableThread;
    private Handler mSurfaceAvailableHandler;
    private boolean m_detectFace = true;
    private boolean mFlipHorizontal = false;
    private String m_zoomFx = null;
    private MediaVideoEncoder mVideoEncoder;
    private boolean mNeedResetEglContext = false;
    private boolean m_isRecording = false;
    private Object mGLThreadSyncObject = new Object();
    private boolean m_frameAvailable = false;
    int m_textureParamHandle;
    int m_textureCoordinateHandle;
    int m_positionHandle;
    int m_textureTranformHandle;
    private int[] mCameraPreviewtextures = new int[1];
    private FloatBuffer textureBuffer;
    private int mDisplayTex = 0;
    int m_convertProgramID = -1;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mGLCubeBuffer;
    private int mPreProcessTextures = -1;
    private int[] mFrameBuffers = null;
    boolean mFlashToggle = false;
    private ArrayList<NvAsset> list = new ArrayList<>();
    private float m_anchorX = 0;
    private float m_anchorY = 0;

    STMobileDetected mStMobileDetected;
    private NvsEffectSdkContext mEffectSdkContext;
    private NvsVideoResolution mCurrentVideoResolution;
    private NvSuperZoom mNvSuperZoom;
    private CenterHorizontalViewAdapter mCenterHorizontalViewAdapter;
    private String fxNames[] = {
            "dramatization",
            "spring",
            "cartoon",
            "daily",
            "dogpacks",
            "fire",
            "horror",
            "love",
            "no",
            "rhythm",
//            "rotate",
            "shake",
            "tragedy",
            "tv",
            "wasted"
    };

    EGLContext mEglContext;
    EGLDisplay mEglDisplay;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FINISH_RECORDING:
                    m_GLView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            // 理解为特效停止
                            mNvSuperZoom.stop();
                        }
                    });
                    // 理解为录制停止
                    stopRecording();
                    Intent intent = new Intent(SuperZoomActivity.this, SuperZoomPreviewActivity.class);
                    intent.putExtra("video_path", mMuxer.getFilePath());
                    intent.putExtra("zoomFx", m_zoomFx);
                    startActivity(intent);
                    clearObjectAnimation();
                    m_buttonRecord.setProgress(0);
                    break;
                case ASSET_LIST_REQUEST_SUCCESS:
                    // 得到可用资源
                    ArrayList<NvAsset> localData = getLocalData(NvAsset.ASSET_SUPER_ZOOM);
                    // 遍历修改list中的数据的状态
                    for (NvAsset nvAsset : localData) {
                        for (int index = 0; index < fxNames.length; index++) {
                            if (TextUtils.equals(list.get(index).uuid, nvAsset.uuid)) {
                                list.get(index).localDirPath = nvAsset.localDirPath;
                                continue;
                            }
                        }
                    }
                    // 更新adapter
                    if (mCenterHorizontalViewAdapter == null) {
                        mCenterHorizontalViewAdapter = new CenterHorizontalViewAdapter(SuperZoomActivity.this, list, 1);
                    } else {
                        mCenterHorizontalViewAdapter.notifyDataSetChanged();
                    }
                    break;
                case ASSET_DOWNLOAD_INPROGRESS:
                    if (mCenterHorizontalViewAdapter != null) {
                        mCenterHorizontalViewAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private void setRecordButtonEnable(boolean enable) {
        m_buttonRecord.setEnabled(enable);
        m_buttonRecord.setClickable(enable);
    }


    private ObjectAnimator animator;
    private NvAssetManager mAssetManager;
    private int mCurPosition = 0;
    private NvAsset m_zoomFxData;

    @Override
    protected int initRootView() {
        return R.layout.activity_super_zoom;
    }

    @Override
    protected void initViews() {
        mEffectSdkContext = NvsEffectSdkContext.getInstance();
        if (mEffectSdkContext == null) {
            String effectSdkLicensePath = "assets:/effectsdkdemo.lic";
            mEffectSdkContext = NvsEffectSdkContext.init(getApplicationContext(), effectSdkLicensePath, 0);
        }
        mStMobileDetected = new STMobileDetected();
        mStMobileDetected.initSTMobileDetected(SuperZoomActivity.this);
        initUI();
        m_GLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mNvSuperZoom = new NvSuperZoom(SuperZoomActivity.this);
            }
        });
    }

    @Override
    protected void initTitle() {
    }

    @Override
    protected List<String> initPermissions() {
        return Util.getAllPermissionsList();
    }

    @Override
    protected void hasPermission() {
    }

    @Override
    protected void nonePermission() {
        Log.d(TAG, "initCapture failed,above 6.0 device may has no access to camera");
        // 拒绝后
        noPermissionDialog();
    }

    @Override
    protected void noPromptPermission() {
        // 拒绝了权限
        Logger.e(TAG, "initCapture failed,above 6.0 device has no access from user");
        noPermissionDialog();
    }

    private void setViewsVisibility(int visibility) {
        m_closeBtn.setVisibility(visibility);
        mToolListLl.setVisibility(visibility);
        mSuperZoomFxView.setVisibility(visibility);
    }

    private void initUI() {
        searchAssetData();
        mToolListLl = (LinearLayout) this.findViewById(R.id.tool_list_ll);
        m_closeBtn = (AppCompatButton) this.findViewById(R.id.close);
        m_closeBtn.setVisibility(View.VISIBLE);
        m_buttonRecord = (RoundProgressView) findViewById(R.id.buttonRecord);
        m_buttonRecord.setProgress(0);
        m_buttonSwitchFacing = (ImageView) findViewById(R.id.buttonSwitchFacing);
        m_buttonFlash = (ImageView) findViewById(R.id.buttonFlash);
        m_GLView = (GLSurfaceView) findViewById(R.id.GLView);

        // 设置使用OPENGL ES2.0
        m_GLView.setEGLContextClientVersion(2);
        m_GLView.setRenderer(this);
        m_GLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mAccelerometer = new Accelerometer(getApplicationContext());

        // 特效选择
        mSuperZoomFxView = (CenterHorizontalView) findViewById(R.id.super_zoom_fx_view);
        mSuperZoomFxView.setHasFixedSize(true);
        mSuperZoomFxView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (mCenterHorizontalViewAdapter == null) {
            mCenterHorizontalViewAdapter = new CenterHorizontalViewAdapter(this, list, 1);
        } else {
            mCenterHorizontalViewAdapter.notifyDataSetChanged();
        }
        // 点击监听
        mCenterHorizontalViewAdapter.setItemClickListener(new CenterHorizontalViewAdapter.ItemClickCallBack() {
            @Override
            public void itemclicked(CenterHorizontalViewAdapter.FxViewHolder holder, int position) {
                // 点击事件 如果没有下载 则下载
                if ((position == mCurPosition) && TextUtils.isEmpty(mCenterHorizontalViewAdapter.getData().get(position).localDirPath)
                        && TextUtils.isEmpty(mCenterHorizontalViewAdapter.getData().get(position).bundledLocalDirPath)) {
                    mAssetManager.downloadAsset(NvAsset.ASSET_SUPER_ZOOM, fxNames[position]);
                    mCenterHorizontalViewAdapter.getData().get(position).downloadStatus = NvAsset.DownloadStatusInProgress;
                }
                mSuperZoomFxView.moveToPosition(position);
                mCurPosition = position;
            }
        });
        mSuperZoomFxView.setAdapter(mCenterHorizontalViewAdapter);
        // 滑动停止后选择
        mSuperZoomFxView.setOnSelectedPositionChangedListener(new CenterHorizontalView.OnSelectedPositionChangedListener() {
            @Override
            public void selectedPositionChanged(int pos) {
                if (mCenterHorizontalViewAdapter.getData().size() > 0) {
                    int i = pos % mCenterHorizontalViewAdapter.getData().size();
                    m_zoomFx = mCenterHorizontalViewAdapter.getData().get(pos).uuid;
                    m_zoomFxData = mCenterHorizontalViewAdapter.getData().get(pos);
                    m_detectFace = true;
                    m_anchorX = 0;
                    m_anchorY = 0;
                }
                mCurPosition = pos;
            }
        });
    }

    @Override
    protected void initData() {
        m_cameraProxy = new CameraProxy(SuperZoomActivity.this);
        m_permissionGranted = false;
        configCameraId();
        checkPermission();
    }

    // 默认为前置，判断有没有前置摄像头
    private void configCameraId() {
        if (m_cameraProxy.getNumberOfCameras() <= 1) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK; // 后置
            ifCanSwitch();
            setFlashButtonEnable(true);
        } else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT; // 前置
            setFlashButtonEnable(false);
        }
    }

    @Override
    protected void initListener() {
        mAssetManager.setManagerlistener(new NvAssetManager.NvAssetManagerListener() {
            @Override
            public void onRemoteAssetsChanged(boolean hasNext) {
                Message updateMessage = mHandler.obtainMessage();
                updateMessage.what = ASSET_LIST_REQUEST_SUCCESS;
                mHandler.sendMessage(updateMessage);
            }

            @Override
            public void onGetRemoteAssetsFailed() {

            }

            @Override
            public void onDownloadAssetProgress(String uuid, int progress) {
                // 下载进度
                for (int index = 0; index < fxNames.length; index++) {
                    if (TextUtils.equals(list.get(index).uuid, uuid)) {
                        list.get(index).downloadProgress = progress;
                        if (mHandler != null) {
                            Message updateMessage = mHandler.obtainMessage();
                            updateMessage.what = ASSET_DOWNLOAD_INPROGRESS;
                            mHandler.sendMessageDelayed(updateMessage, 100);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onDonwloadAssetFailed(String uuid) {
                for (int index = 0; index < fxNames.length; index++) {
                    if (TextUtils.equals(list.get(index).uuid, uuid)) {
                        list.get(index).downloadStatus = NvAsset.DownloadStatusFailed;
                        Message updateMessage = mHandler.obtainMessage();
                        updateMessage.what = ASSET_DOWNLOAD_INPROGRESS;
                        updateMessage.arg1 = index;
                        mHandler.sendMessage(updateMessage);
                        return;
                    }
                }
            }

            @Override
            public void onDonwloadAssetSuccess(String uuid) {
                // 下在完后重新更新
                ArrayList<NvAsset> localData = getLocalData(NvAsset.ASSET_SUPER_ZOOM);
                for (NvAsset nvAsset : localData) {
                    for (int index = 0; index < fxNames.length; index++) {
                        if (TextUtils.equals(list.get(index).uuid, nvAsset.uuid)) {
                            list.get(index).localDirPath = nvAsset.localDirPath;
                            list.get(index).downloadStatus = NvAsset.DownloadStatusDecompressing;
                            continue;
                        }
                    }
                }
                Message updateMessage = mHandler.obtainMessage();
                updateMessage.what = ASSET_DOWNLOAD_INPROGRESS;
                mHandler.sendMessage(updateMessage);
            }

            @Override
            public void onFinishAssetPackageInstallation(String uuid) {
                // 解压完成
                ArrayList<NvAsset> localData = getLocalData(NvAsset.ASSET_SUPER_ZOOM);
                for (NvAsset nvAsset : localData) {
                    for (int index = 0; index < fxNames.length; index++) {
                        if (TextUtils.equals(list.get(index).uuid, nvAsset.uuid)) {
                            list.get(index).localDirPath = nvAsset.localDirPath;
                            list.get(index).downloadStatus = NvAsset.DownloadStatusFinished;
                            continue;
                        }
                    }
                }
                Message updateMessage = mHandler.obtainMessage();
                updateMessage.what = ASSET_DOWNLOAD_INPROGRESS;
                mHandler.sendMessage(updateMessage);
            }

            @Override
            public void onFinishAssetPackageUpgrading(String uuid) {

            }
        });
        m_buttonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_buttonFlash.isEnabled()) {
                    if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        return;
                    }
                    mFlashToggle = !mFlashToggle;
                    m_cameraProxy.toggleFlash(mFlashToggle);
                    m_buttonFlash.setBackground(null);
                    if (mFlashToggle) {
                        m_buttonFlash.setBackgroundResource(R.mipmap.icon_flash_on);
                    } else {
                        m_buttonFlash.setBackgroundResource(R.mipmap.icon_flash_off);
                    }
                }
            }
        });

        m_closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getInstance().finishActivity(SuperZoomActivity.class);
            }
        });
        // 点击录制
        m_buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_isRecording) {
                    setRecordButtonEnable(false);
                    return;
                }
                // 判断有无素材
                for (int index = 0; index < fxNames.length; index++) {
                    if (TextUtils.equals(list.get(index).uuid, m_zoomFx)) {
                        if (TextUtils.isEmpty(list.get(index).localDirPath) && TextUtils.isEmpty(list.get(index).bundledLocalDirPath)) {
                            //无资源
                            String[] versionName = getResources().getStringArray(R.array.super_zoom_resouce_tips);
                            Util.showDialog(SuperZoomActivity.this, versionName[0], versionName[1]);
                            return;
                        }
                    }
                }
                // 人脸检测
                m_detectFace = false;
                m_GLView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        // 开始特效
                        if (m_zoomFxData != null && !TextUtils.isEmpty(m_zoomFxData.localDirPath)) {
                            mNvSuperZoom.setAssetsExternalPath(PathUtils.getAssetDownloadPath(NvAsset.ASSET_SUPER_ZOOM));
                        } else if (m_zoomFxData != null && !TextUtils.isEmpty(m_zoomFxData.bundledLocalDirPath)) {
                            mNvSuperZoom.setAssetsExternalPath(null);
                        }
                        mNvSuperZoom.start(m_zoomFx, mCurrentVideoResolution.imageWidth, mCurrentVideoResolution.imageHeight, m_anchorX, m_anchorY);
                    }
                });
                //开始动画
                startRecordProgress();
                // 理解为开始录制
                startRecording();
            }
        });
        // 未录制状态点击视图
        m_GLView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!m_isRecording) {
                    m_detectFace = false;
                    m_anchorX = event.getX() / m_GLView.getWidth() - 0.5f;
                    m_anchorY = -(event.getY() / m_GLView.getHeight() - 0.5f);
                }
                return true;
            }
        });
        // 切换摄像头
        m_buttonSwitchFacing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_cameraProxy.getNumberOfCameras() <= 1) {
                    return;
                }
                if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // 闪光灯标示不可用
                    setFlashButtonEnable(false);
                    mFlashToggle = false;
                } else if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    // 闪光灯标示可用
                    setFlashButtonEnable(true);
                }
                switchCamera();
            }
        });
    }

    private void setFlashButtonEnable(boolean enable) {
        m_buttonFlash.setBackground(null);
        if (enable) {
            m_buttonFlash.setAlpha(1.0f);
            m_buttonFlash.setBackgroundResource(R.mipmap.icon_flash_off);
            m_buttonFlash.setClickable(true);
            m_buttonFlash.setEnabled(true);
        } else {
            m_buttonFlash.setAlpha(0.5f);
            m_buttonFlash.setBackgroundResource(R.mipmap.icon_flash_off);
            m_buttonFlash.setClickable(false);
            m_buttonFlash.setEnabled(false);
        }
    }

    private NvAsset createSuperZoomEffectItem(String fxName) {
        NvAsset item = new NvAsset();
        if (TextUtils.equals("dramatization", fxName) || TextUtils.equals("spring", fxName)) {
            item.bundledLocalDirPath = "file:///android_asset/meicam";
        }
        item.uuid = fxName;
        return item;
    }

    private void startRecordProgress() {
        if (animator == null) {
            animator = ObjectAnimator.ofInt(m_buttonRecord, "progress", 0, 100);
            animator.setDuration(4080);
            animator.start();
        }
    }

    private void clearObjectAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    // 初始化推镜特效列表
    private void searchAssetData() {
        //  搜索本地数据数据 检索数据后 判断是不是本地存在  结束后刷新adapter
        //  点击下载后 直接更新adapter 随后存储数据（等待下一次app启动的时候，进行本地搜索）
        for (String fxName : fxNames) {
            list.add(createSuperZoomEffectItem(fxName));
        }
        // 搜索本地存储文件 更新list中记录下载状态
        mAssetManager = NvAssetManager.sharedInstance();
       /* String bundlePath = "meicam";
        // 搜索预装
        mAssetManager.searchReservedAssets(NvAsset.ASSET_SUPER_ZOOM, bundlePath);*/
        // 搜索本地（已经下载） 只搜索一次
        mAssetManager.searchLocalAssets(NvAsset.ASSET_SUPER_ZOOM);
        // 得到可用资源
        ArrayList<NvAsset> localData = getLocalData(NvAsset.ASSET_SUPER_ZOOM);
        // 遍历修改list中的数据的状态
        for (NvAsset nvAsset : localData) {
            for (int index = 0; index < fxNames.length; index++) {
                if (TextUtils.equals(list.get(index).uuid, nvAsset.uuid)) {
                    list.get(index).localDirPath = nvAsset.localDirPath;
                    continue;
                }
            }
        }
        mAssetManager.downloadRemoteAssetsInfo(NvAsset.ASSET_SUPER_ZOOM, NvAsset.AspectRatio_All, 0, 0, 20);
    }

    private ArrayList<NvAsset> getLocalData(int assetType) {
        return mAssetManager.getUsableAssets(assetType, NvAsset.AspectRatio_All, 0);
    }

    // 选择摄像头
    private boolean setupCamera() {
        if (m_cameraProxy.getCamera() == null) {
            if (m_cameraProxy.getNumberOfCameras() == 1) {
                mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
        m_cameraProxy.stopPreview();
        if (!m_cameraProxy.openCamera(mCameraID)) {
            Log.d(TAG, "no camera permission , can't open camera");
            return false;
        }
        boolean isHuaweiP6 = checkMobileModel();
        if (isHuaweiP6) {
            m_cameraProxy.setPreviewSize(640, 480);
        } else {
            m_cameraProxy.setPreviewSize(1280, 720);
        }
        m_oritation = Accelerometer.getDisplayOrientation(this, m_cameraProxy.getCameraId());
        return true;
    }

    private boolean checkMobileModel() {
        String model = Build.MODEL;
        String manufaturer = Build.MANUFACTURER;
        if (model == null || manufaturer == null) {
            return false;
        }
        boolean isHuaweoP6 = false;
        model = model.toUpperCase();
        manufaturer = manufaturer.toUpperCase();
        if (model.equals("HUAWEI P6-C00") && manufaturer.equals("HUAWEI")) {
            isHuaweoP6 = true;
        }
        return isHuaweoP6;
    }

    // 切换摄像头
    public void switchCamera() {
        if (m_cameraProxy.cameraOpenFailed()) {
            return;
        }
        synchronized (mGLThreadSyncObject) {
            m_isPreviewing = false;
        }
        mCameraID = 1 - mCameraID;
        startCapturePreview();
    }

    private void checkPermission() {
        // 6.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        m_permissionGranted = true;
                        setCaptureEnabled(true);
                        startCapturePreview();
                    } else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                    }
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            }
        } else {
            // 6.0以下
            m_permissionGranted = true;
            startCapturePreview();
        }
    }

    // 无权限界面不可用
    private void setCaptureEnabled(boolean enabled) {
        m_buttonRecord.setEnabled(enabled);
        m_buttonSwitchFacing.setEnabled(enabled);
        m_buttonFlash.setEnabled(enabled);
    }

    // 设置切换摄像头是否可用
    private void ifCanSwitch() {
        if (m_cameraProxy.getNumberOfCameras() > 1) {
            m_buttonSwitchFacing.setClickable(true);
            m_buttonSwitchFacing.setEnabled(true);
            m_buttonSwitchFacing.setAlpha(1.0F);
        } else {
            m_buttonSwitchFacing.setClickable(false);
            m_buttonSwitchFacing.setEnabled(false);
            m_buttonSwitchFacing.setAlpha(0.5F);
        }
    }

    // 开始预览
    private boolean startCapturePreview() {
        if (!m_permissionGranted || m_isPreviewing || m_cameraPreviewTexture == null)
            return false;

        if (setupCamera()) {
            // 有权限(根据是否打开摄像头同时判断时候有摄像头权限)
            setCaptureEnabled(true);
            Log.d(TAG, "below 6.0 devices has access");
        } else {
            // 无权限
            setCaptureEnabled(false);
            noPermissionDialog();
            Log.d(TAG, "below 6.0 devices has no access");
            return false;
        }
        m_cameraProxy.startPreview(m_cameraPreviewTexture, mPreviewCallback);
        Camera.Size size = m_cameraProxy.getPreviewSize();
        mCurrentVideoResolution = new NvsVideoResolution();
        if (m_oritation == 90 || m_oritation == 270) {
            mCurrentVideoResolution.imageWidth = size.height;
            mCurrentVideoResolution.imageHeight = size.width;
        } else {
            mCurrentVideoResolution.imageWidth = size.width;
            mCurrentVideoResolution.imageHeight = size.height;
        }
        mCurrentVideoResolution.imagePAR = new NvsRational(1, 1);
        mFlipHorizontal = m_cameraProxy.isFlipHorizontal();
        synchronized (mGLThreadSyncObject) {
            m_isPreviewing = true;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void eglCreateContex() {
        EGLContext currentContext = EGL14.eglGetCurrentContext();
        EGLSurface currentSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        EGL14.eglMakeCurrent(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);

        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "eglGetDisplay failed");
            return;
        }

        final int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            mEglDisplay = null;
            Log.e(TAG, "eglInitialize failed");
            return;
        }

        android.opengl.EGLConfig mEglConfig = getConfig(false, true, mEglDisplay);
        if (mEglConfig == null) {
            Log.e(TAG, "chooseConfig failed");
            return;
        }
        // create EGL rendering context
        final int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, currentContext, attrib_list, 0);
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            Log.e(TAG, "eglCreateContext");
        }

        if (!EGL14.eglMakeCurrent(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), currentSurface, currentSurface, currentContext)) {
            Log.e(TAG, "eglMakeCurrent failed");
        }
    }

    private void setupGraphics() {
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(this, R.raw.vetext_sharder);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(this, R.raw.fragment_sharder);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        m_shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});

        GLES20.glUseProgram(m_shaderProgram);
        m_textureParamHandle = GLES20.glGetUniformLocation(m_shaderProgram, "texture");
        m_textureCoordinateHandle = GLES20.glGetAttribLocation(m_shaderProgram, "vTexCoordinate");
        m_positionHandle = GLES20.glGetAttribLocation(m_shaderProgram, "vPosition");
        m_textureTranformHandle = GLES20.glGetUniformLocation(m_shaderProgram, "textureTransform");
    }

    private void setupVertexBuffer() {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(m_drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        m_drawListBuffer = dlb.asShortBuffer();
        m_drawListBuffer.put(m_drawOrder);
        m_drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(m_squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        m_vertexBuffer = bb.asFloatBuffer();
        m_vertexBuffer.put(m_squareCoords);
        m_vertexBuffer.position(0);
    }

    private void setupTexture() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        glGenTextures(1, mCameraPreviewtextures, 0);
        EGLHelper.checkGlError("Texture generate");

        if (mSurfaceAvailableThread == null) {
            mSurfaceAvailableThread = new HandlerThread("ProcessImageThread");
            mSurfaceAvailableThread.start();
            mSurfaceAvailableHandler = new Handler(mSurfaceAvailableThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                }
            };
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraPreviewtextures[0]);
        //创建摄像机需要的Preview Texture
        m_cameraPreviewTexture = new SurfaceTexture(mCameraPreviewtextures[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            m_cameraPreviewTexture.setOnFrameAvailableListener(this, mSurfaceAvailableHandler);
        else
            m_cameraPreviewTexture.setOnFrameAvailableListener(this);

        m_cameraPreviewTexture.setOnFrameAvailableListener(this);
    }

    private void drawTextureOES(int displayTex, int texWidht, int texHeight) {
        // Draw texture

        GLES20.glUseProgram(m_shaderProgram);

        float ar = (float) texWidht / texHeight;
        float disar = (float) m_width / m_height;
        float cropWidth = 1.0f;
        float cropHeight = (float) 1.0;
        if (ar > disar) {
            cropHeight = 1.0f;
            cropWidth = ar / disar;
        } else {
            cropWidth = 1.0f;
            cropHeight = disar / ar;
        }
        m_vertexBuffer.put(0, -cropWidth);
        m_vertexBuffer.put(1, cropHeight);
        m_vertexBuffer.put(2, -cropWidth);
        m_vertexBuffer.put(3, -cropHeight);
        m_vertexBuffer.put(4, cropWidth);
        m_vertexBuffer.put(5, -cropHeight);
        m_vertexBuffer.put(6, cropWidth);
        m_vertexBuffer.put(7, cropHeight);
        m_vertexBuffer.position(0);

        GLES20.glEnableVertexAttribArray(m_positionHandle);
        GLES20.glVertexAttribPointer(m_positionHandle, 2, GLES20.GL_FLOAT, false, 0, m_vertexBuffer);

        EGLHelper.checkGlError("glEnableVertexAttribArray");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, displayTex);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(m_textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(m_textureCoordinateHandle);
        GLES20.glVertexAttribPointer(m_textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glUniformMatrix4fv(m_textureTranformHandle, 1, false, m_videoTextureTransform, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, m_drawOrder.length, GLES20.GL_UNSIGNED_SHORT, m_drawListBuffer);
        GLES20.glDisableVertexAttribArray(m_positionHandle);
        GLES20.glDisableVertexAttribArray(m_textureCoordinateHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                noPermissionDialog();
                return;
            }
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION_CODE:
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            m_permissionGranted = true;
                            startCapturePreview();
                        } else
                            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                    } else {
                        requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                    }
                    break;
                case REQUEST_RECORD_AUDIO_PERMISSION_CODE:
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        m_permissionGranted = true;
                        startCapturePreview();
                    } else
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                    break;
                case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE:
                    m_permissionGranted = true;
                    startCapturePreview();
                    break;
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            m_frameAvailable = true;
        }
        m_GLView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        eglCreateContex();
        setupGraphics();
        setupVertexBuffer();
        setupTexture();
        //在UI线程中创建timeline，SDK中绝大部分函数都应在UI线程调用
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startCapturePreview();
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        m_width = width;
        m_height = height;
        GLES20.glViewport(0, 0, width, height);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onDrawFrame(GL10 gl) {
        DrawFrameToGLView();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void DrawFrameToGLView() {
        //计算当前的时间戳
        synchronized (this) {
            if (m_frameAvailable) {
                m_cameraPreviewTexture.updateTexImage();
                m_cameraPreviewTexture.getTransformMatrix(m_videoTextureTransform);
                m_frameAvailable = false;
            }
        }
        boolean isInPreview = false;
        synchronized (mGLThreadSyncObject) {
            isInPreview = m_isPreviewing;
        }

        int texWidth = m_width;
        int texHeight = m_height;
        mDisplayTex = mCameraPreviewtextures[0];

        if (isInPreview && mNvSuperZoom != null) {
            texWidth = mCurrentVideoResolution.imageWidth;
            texHeight = mCurrentVideoResolution.imageHeight;
            if (m_isRecording) {
                long timestamp = m_cameraPreviewTexture.getTimestamp();
                mDisplayTex = mNvSuperZoom.render(mCameraPreviewtextures[0], true,
                        m_cameraProxy.getOrientation(), mFlipHorizontal);
                if (m_zoomFx != null) {
                    if (mNvSuperZoom.renderEnded()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopRecording();
                            }
                        });
                    }
                } else {
                    mDisplayTex = preProcess(mCameraPreviewtextures[0], texWidth, texHeight, m_cameraProxy.getOrientation(), mFlipHorizontal);
                }
                GLES20.glFinish();
                synchronized (this) {
                    if (mVideoEncoder != null) {
                        if (mNeedResetEglContext) {
                            mVideoEncoder.setEglContext(mEglContext, mDisplayTex);
                            mNeedResetEglContext = false;
                        }
                        mVideoEncoder.frameAvailableSoon(timestamp);
                    }
                }
            }
        }
        GLES20.glViewport(0, 0, m_width, m_height);
        if (mDisplayTex == mCameraPreviewtextures[0])
            this.drawTextureOES(mDisplayTex, texWidth, texHeight);
        else {
            if (mNvSuperZoom != null)
                drawTexture(mDisplayTex, texWidth, texHeight, m_width, m_height);
        }
        EGLHelper.checkGlError("drawTexture");
    }

    private int m_drawTextureProgramID = -1;
    private FloatBuffer mDrawTextureBuffer;
    private FloatBuffer mDrawGLCubeBuffer;

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private android.opengl.EGLConfig getConfig(final boolean with_depth_buffer, final boolean isRecordable, EGLDisplay mEglDisplay) {
        final int[] attribList = {
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //EGL_RECORDABLE_ANDROID, 1,	// this flag need to recording of MediaCodec
                EGL14.EGL_NONE, EGL14.EGL_NONE,    //	with_depth_buffer ? EGL14.EGL_DEPTH_SIZE : EGL14.EGL_NONE,
                // with_depth_buffer ? 16 : 0,
                EGL14.EGL_NONE
        };
        int offset = 10;
        if (false) {
            attribList[offset++] = EGL14.EGL_STENCIL_SIZE;
            attribList[offset++] = 8;
        }
        if (with_depth_buffer) {
            attribList[offset++] = EGL14.EGL_DEPTH_SIZE;
            attribList[offset++] = 16;
        }
        if (isRecordable && (Build.VERSION.SDK_INT >= 18)) {
            attribList[offset++] = EGL_RECORDABLE_ANDROID;
            attribList[offset++] = 1;
        }
        for (int i = attribList.length - 1; i >= offset; i--) {
            attribList[i] = EGL14.EGL_NONE;
        }
        final android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
        final int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0)) {
            // XXX it will be better to fallback to RGB565
            Log.w(TAG, "unable to find RGBA8888 / " + " EGLConfig");
            return null;
        }
        return configs[0];
    }

    /**
     * 输入结果到GLSurfaceView，必须在opengl环境中运行
     */
    public void drawTexture(int displayTex, int texWidth, int texHeight, int displayWidth, int displayHeight) {
        // Draw texture
        if (m_drawTextureProgramID < 0) {
            m_drawTextureProgramID = EGLHelper.loadProgramForTexture();
            mDrawGLCubeBuffer = ByteBuffer.allocateDirect(EGLHelper.CUBE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mDrawGLCubeBuffer.put(EGLHelper.CUBE).position(0);
            mDrawTextureBuffer = ByteBuffer.allocateDirect(EGLHelper.TEXTURE_NO_ROTATION.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mDrawTextureBuffer.clear();
            mDrawTextureBuffer.put(EGLHelper.TEXTURE_NO_ROTATION).position(0);
        }
        float ar = (float) texWidth / texHeight;
        float disar = (float) displayWidth / displayHeight;
        float cropWidth = 1.0f;
        float cropHeight = (float) 1.0;
        if (ar > disar) {
            cropHeight = 1.0f;
            cropWidth = ar / disar;
        } else {
            cropWidth = 1.0f;
            cropHeight = disar / ar;
        }
        mDrawGLCubeBuffer.put(0, -cropWidth);
        mDrawGLCubeBuffer.put(1, cropHeight);
        mDrawGLCubeBuffer.put(2, cropWidth);
        mDrawGLCubeBuffer.put(3, cropHeight);
        mDrawGLCubeBuffer.put(4, -cropWidth);
        mDrawGLCubeBuffer.put(5, -cropHeight);
        mDrawGLCubeBuffer.put(6, cropWidth);
        mDrawGLCubeBuffer.put(7, -cropHeight);
        mDrawGLCubeBuffer.position(0);
        GLES20.glUseProgram(m_drawTextureProgramID);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, displayTex);

        mDrawGLCubeBuffer.position(0);
        int glAttribPosition = GLES20.glGetAttribLocation(m_drawTextureProgramID, POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mDrawGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mDrawTextureBuffer.position(0);
        int glAttribTextureCoordinate = GLES20.glGetAttribLocation(m_drawTextureProgramID, TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                mDrawTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        int textUniform = GLES20.glGetUniformLocation(m_drawTextureProgramID, TEXTURE_UNIFORM);
        GLES20.glUniform1i(textUniform, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            if (m_detectFace) {
                int cameraOrientation = m_cameraProxy.getOrientation();
                int bufferWidth = mCurrentVideoResolution.imageWidth;
                int bufferHeight = mCurrentVideoResolution.imageHeight;
                if (cameraOrientation == 90 || cameraOrientation == 270) {
                    bufferWidth = mCurrentVideoResolution.imageHeight;
                    bufferHeight = mCurrentVideoResolution.imageWidth;
                }
                STHumanAction humanAction = mStMobileDetected.stMobileDetected(data, bufferWidth, bufferHeight, cameraOrientation, mFlipHorizontal);
                Rect rect = mStMobileDetected.getFaceRect(humanAction);
                if (rect != null) {
                    float centerX = (rect.right + rect.left) / 2;
                    float centerY = (rect.bottom + rect.top) / 2;
                    m_anchorX = centerX - 0.5f;
                    m_anchorY = -centerY + 0.5f;
                }
            }
        }
    };

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder((MediaVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder(null);
        }
    };

    private MediaMuxerWrapper mMuxer;

    private void startRecording() {
        setViewsVisibility(View.INVISIBLE);
        mNeedResetEglContext = true;
        try {
            mMuxer = new MediaMuxerWrapper(".mp4");
            mMuxer.mHandler = mHandler;
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCurrentVideoResolution.imageWidth, mCurrentVideoResolution.imageHeight);
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
        m_isRecording = true;
    }

    private void stopRecording() {
        if (!m_isRecording)
            return;
        m_isRecording = false;
        if (mMuxer != null) {
            mMuxer.stopRecording();
        }
        setViewsVisibility(View.VISIBLE);
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        m_GLView.queueEvent(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                synchronized (this) {
                    if (encoder != null && mDisplayTex > 0) {
                        encoder.setEglContext(mEglContext, mDisplayTex);
                    }
                    mVideoEncoder = encoder;
                }
            }
        });
    }

    private int preProcess(int textureId, int width, int height, int cameraOrientation, boolean flipHorizontal) {
        if (m_convertProgramID <= 0) {
            m_convertProgramID = EGLHelper.loadProgramForSurfaceTexture();

            mGLCubeBuffer = ByteBuffer.allocateDirect(EGLHelper.CUBE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mGLCubeBuffer.put(EGLHelper.CUBE).position(0);

            mTextureBuffer = ByteBuffer.allocateDirect(EGLHelper.TEXTURE_NO_ROTATION.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            mTextureBuffer.clear();
            mTextureBuffer.put(EGLHelper.TEXTURE_NO_ROTATION).position(0);
        }


        if (m_convertProgramID < 0)
            return -1;

        float[] textureCords = EGLHelper.getRotation(cameraOrientation, true, flipHorizontal);
        mTextureBuffer.clear();
        mTextureBuffer.put(textureCords).position(0);
        EGLHelper.checkGlError("preProcess");

        GLES20.glUseProgram(m_convertProgramID);
        EGLHelper.checkGlError("glUseProgram");

        if (mPreProcessTextures <= 0) {
            mPreProcessTextures = createGLTexture(width, height);
            EGLHelper.bindFrameBuffer(mPreProcessTextures, mFrameBuffers[0], width, height);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mPreProcessTextures);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mPreProcessTextures, 0);

        mGLCubeBuffer.position(0);
        int glAttribPosition = GLES20.glGetAttribLocation(m_convertProgramID, POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);
        EGLHelper.checkGlError("glEnableVertexAttribArray");

        mTextureBuffer.clear();
        int glAttribTextureCoordinate = GLES20.glGetAttribLocation(m_convertProgramID, TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);
        EGLHelper.checkGlError("glEnableVertexAttribArray");

        if (textureId != -1) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            int textUniform = GLES20.glGetUniformLocation(m_convertProgramID, TEXTURE_UNIFORM);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(textUniform, 0);
            EGLHelper.checkGlError("glBindTexture");
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        EGLHelper.checkGlError("glBindTexture");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);

        return mPreProcessTextures;
    }

    private int createGLTexture(int width, int height) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        int[] tex = new int[1];
        glGenTextures(1, tex, 0);
        EGLHelper.checkGlError("Texture generate");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        if (mFrameBuffers == null) {
            mFrameBuffers = new int[1];
            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        }

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        EGLHelper.bindFrameBuffer(tex[0], mFrameBuffers[0], width, height);

        return tex[0];
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecordButtonEnable(true);
        clearObjectAnimation();
        mAccelerometer.start();
        if (m_cameraPreviewTexture != null)
            startCapturePreview();
    }

    @Override
    protected void onPause() {
        if (m_isRecording) {
            m_GLView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    // 理解为特效停止
                    mNvSuperZoom.stop();
                }
            });
            // 理解为录制停止
            stopRecording();
        }
        m_isRecording = false;
        mFlashToggle = false;
        if (m_cameraProxy != null)
            m_cameraProxy.releaseCamera();
        //停止引擎
        synchronized (mGLThreadSyncObject) {
            m_isPreviewing = false;
        }
        final CountDownLatch count = new CountDownLatch(1);
        m_GLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                count.countDown();
            }
        });

        try {
            count.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAccelerometer != null)
            mAccelerometer.stop();
        clearObjectAnimation();
        // 修正摄像头状态
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            m_buttonFlash.setBackgroundResource(R.mipmap.icon_flash_off);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_isRecording = false;
    }

    @Override
    protected void onDestroy() {
        mAccelerometer = null;
        if (mStMobileDetected != null) {
            mStMobileDetected.closeDetected();
            mStMobileDetected = null;
        }
        m_GLView.queueEvent(new Runnable() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                if (mFrameBuffers != null) {
                    GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
                    mFrameBuffers = null;
                }
                if (m_convertProgramID > 0) {
                    GLES20.glDeleteProgram(m_convertProgramID);
                }
                m_convertProgramID = -1;
                if (m_drawTextureProgramID > 0) {
                    GLES20.glDeleteProgram(m_drawTextureProgramID);
                }
                m_drawTextureProgramID = -1;
                mNvSuperZoom.releaseResources();
                mNvSuperZoom = null;
                if (mEglContext != null) {
                    EGL14.eglDestroyContext(mEglDisplay, mEglContext);
                    mEglDisplay = null;
                    mEglContext = null;
                }
            }
        });
        if (mEffectSdkContext != null) {
            NvsEffectSdkContext.close();
            mEffectSdkContext = null;
        }
        if (m_cameraProxy != null) {
            m_cameraProxy.releaseCamera();
            m_cameraProxy = null;
        }
        if (mHandler != null) {
            mHandler.removeMessages(ASSET_DOWNLOAD_INPROGRESS);
            mHandler = null;
        }
        cancelDownloadTask();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        m_GLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // 理解为特效停止
                mNvSuperZoom.stop();
            }
        });
        if (mMuxer != null && mMuxer.mHandler != null) {
            mMuxer.mHandler = null;
        }
        stopRecording();
        cancelDownloadTask();
        super.onBackPressed();
    }

    public void cancelDownloadTask() {
        ArrayList<String> pendingAssetsToDownloads = mAssetManager.getPendingAssetsToDownload();
        for (String uuid : pendingAssetsToDownloads) {
            mAssetManager.cancelAssetDownload(uuid);
        }
    }

    private void noPermissionDialog() {
        String[] permissionsTips = getResources().getStringArray(R.array.permissions_tips);
        Util.showDialog(SuperZoomActivity.this, permissionsTips[0], permissionsTips[1], new TipsButtonClickListener() {
            @Override
            public void onTipsButtoClick(View view) {
                AppManager.getInstance().finishActivity();
            }
        });
    }

}