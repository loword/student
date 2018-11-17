package com.supe.stu.video;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.ar.TakePictureCallback;
import com.baidu.ar.bean.ARResource;
import com.baidu.ar.face.FaceAr;
import com.baidu.ar.face.FaceArSettings;
import com.baidu.ar.face.FaceArStatusChangeListener;
import com.baidu.ar.facear.FrameInfo;
import com.baidu.ar.recorder.MovieRecorderCallback;
import com.baidu.ar.rotate.Orientation;
import com.baidu.ar.rotate.OrientationManager;
import com.baidu.ar.util.Res;
import com.baidu.ar.util.SystemInfoUtil;
import com.baidu.ar.util.UiThreadUtil;
import com.supe.stu.video.camera.ARCameraOperationCallback;
import com.supe.stu.video.draw.ARRenderer;
import com.supe.stu.video.ui.FaceBottomMenuMgr;
import com.supe.stu.video.ui.FaceBoxView;
import com.supe.stu.video.ui.FaceRes;
import com.supe.stu.video.ui.TitleBarUIMgr;
import com.supe.stu.video.ui.rotateview.RotateImageView;
import com.supe.stu.video.ui.rotateview.RotateViewUtils;
import com.supe.stu.video.utils.FacerARAssetsManager;


import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hanyong on 2018/7/19.
 */

public class FaceUI implements View.OnClickListener, ARCameraOperationCallback,
        FaceBottomMenuMgr.OnFaceMenuChangeListener, FaceArStatusChangeListener {

    private static final int HANDLE_POST_DELAY_MSG = 100001;
    private static final int HANDLE_POST_DELAY_BG_GONE_MSG = 100003;

    private static final String CAPTURE_DIR = "/sdcard/faceDCIM/";
    private ARActivity mActivity;
    private ARRenderer mARRenderer;

    private OrientationManager mOrientationManager = null;
    /**
     * 切换摄像头监听
     */
    private ARSwitchCameraCallback mARSwitchCameraCallback;

    private View mRootView;
    // camera preview
    private GLSurfaceView mGLSurfaceView;
    // 返回按钮
    private RotateImageView mIconBack;
    // 切换摄像头按钮
    private RotateImageView mIconCamera;
    // 闪光灯按钮
    private RotateImageView mIconFlash;

    private ViewGroup mBottomViewGroup;

    private FaceBoxView mBdarFaceBox;

    private View mProgressBar;

    // 贴纸按钮
    Button mStickerBtn;
    // 美颜按钮
    View mBeautyBtn;
    // 拍照按钮

    View mPicBtn;
    // 录像按钮
    View mVideoBtn;

    // 动画触发方式提示
    private RotateImageView mFacetipView;

    // 闪光灯是否处于关闭模式
    private boolean mIsFlashOff = true;
    // 当前是后置摄像头
    private boolean mBackCamera = false;
    private boolean isRelease = false;
    private boolean hasLoadMask = false;
    private String currentTrigger;

    TitleBarUIMgr mTitleBarUIMgr;
    FaceBottomMenuMgr mFaceBottomMenuMgr;

    private boolean mFaceBoxUIStateFlag;

    private FacerARAssetsManager mFacerARAssetsManager;

    private RecordStatus mRecordStatus = RecordStatus.NOT_CAPTURING;

    private Handler mFaceBoxUIandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case HANDLE_POST_DELAY_MSG:
                    mBdarFaceBox.setVisibility(View.GONE);
                    break;

                default:
            }
            return false;
        }
    });

    public FaceUI(ARActivity activity, boolean isBackCamera) {
        mActivity = new WeakReference<ARActivity>(activity).get();
        mOrientationManager = new OrientationManager(mActivity.getApplicationContext());
        mARRenderer = new ARRenderer(SystemInfoUtil.isScreenOrientationLandscape(activity));
        mARRenderer.setARFrameListener(
                new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        mGLSurfaceView.requestRender();
                    }
                }
        );
        initView();
        mBackCamera = isBackCamera;
        mActivity.getARCameraManager().setOperationcallback(this);

        mTitleBarUIMgr = new TitleBarUIMgr(this);
        mTitleBarUIMgr.setShowRecordState(false);
        mFaceBottomMenuMgr = new FaceBottomMenuMgr(this);
        mFaceBottomMenuMgr.setFaceMenuChangeListener(this);
    }

    private void initView() {
        mRootView = mActivity.findViewById(R.id.bdar_id_fragment_container);
        mGLSurfaceView = mActivity.findViewById(R.id.bdar_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(mARRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mIconBack = mActivity.findViewById(R.id.bdar_titlebar_back);
        mIconCamera = mActivity.findViewById(R.id.bdar_titlebar_camera);
        mIconFlash = mActivity.findViewById(R.id.bdar_titlebar_flash);

        // 判断是否有双光灯 没有隐藏
        if (!SystemInfoUtil.checkFlashFeature(mActivity)) {
            mIconFlash.setVisibility(View.GONE);
        }

        mBdarFaceBox = mActivity.findViewById(Res.id("bdar_face_box_layer"));
        mBottomViewGroup = mActivity.findViewById(R.id.bdar_face_bottom);
        mStickerBtn = mActivity.findViewById(R.id.bdar_id_sticker_layout);
        mBeautyBtn = mActivity.findViewById(R.id.bdar_id_beauty_layout);
        mFacetipView = mActivity.findViewById(R.id.facear_tip_view);

        mProgressBar = mActivity.findViewById(R.id.bdar_pb_state);
        mVideoBtn = mActivity.findViewById(R.id.bdar_id_video_layout);
        mPicBtn = mActivity.findViewById(R.id.bdar_id_pic_layout);
    }

    public void onReady() {
        mIconBack.setOnClickListener(this);
        mIconCamera.setOnClickListener(this);
        mIconFlash.setOnClickListener(this);
        mVideoBtn.setOnClickListener(this);
        mPicBtn.setOnClickListener(this);
    }

    public void showFaceBox() {
        getARActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PointF focusPoint = mBdarFaceBox.getCenterPoint();

                if (focusPoint == null) {
                    return;
                }
                mFaceBoxUIandler.removeCallbacksAndMessages(null);
                mBdarFaceBox.setVisibility(View.VISIBLE);
                senMessage(HANDLE_POST_DELAY_MSG, 2000); // 2s后隐藏人脸框
            }
        });
    }

    private void senMessage(int msgId, long delayMills) {
        Message message = mFaceBoxUIandler.obtainMessage();
        message.what = msgId;
        if (delayMills == -1) {
            mFaceBoxUIandler.sendMessage(message);
        } else {
            mFaceBoxUIandler.sendMessageDelayed(message, delayMills);
        }
    }

    public void activityFinishPre() {
        if (mRecordStatus == RecordStatus.CAPTURING) {
            mRecordStatus = RecordStatus.STOPPING;
            stopRecord();
        }

        if (!mIsFlashOff) {
            closeFlash();
            mIsFlashOff = true;
        }

    }

    public void loadFaceAssets(ARResource arResource) {
        mActivity.getARController().setFaceARStatusChangedListener(this);
        mFacerARAssetsManager = new FacerARAssetsManager(mActivity);
        mFacerARAssetsManager.requestFacerArModel(new FacerARAssetsManager.FacerArAssetCallBack() {
            @Override
            public void onResult(String modelPath) {
                if (isReleased()) {
                    return;
                }
                if (TextUtils.isEmpty(modelPath)) {
                    // 黑名单逻辑
                    AlertDialog.Builder builder = new AlertDialog.Builder(getARActivity());
                    builder.setTitle("提示");
                    builder.setMessage("该机型暂不在支持范围，后续将开放更多机型权限，敬请关注");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getARActivity().finish();
                        }

                    });

                    builder.setCancelable(false);
                    builder.show();
                    return;
                }
                mActivity.getARController().setTrackingModel(modelPath);
            }
        });

        mStickerBtn.setText("资源处理中...点击无响应");
        mFacerARAssetsManager.requestFacerArMask(new FacerARAssetsManager.FacerArAssetCallBack() {
            @Override
            public void onResult(String path) {
                mStickerBtn.setText("贴纸(可点击)");
                mFaceBottomMenuMgr.updateMaskView(path);
                mStickerBtn.setOnClickListener(FaceUI.this);

                mFacerARAssetsManager.requestFacerArConfig(new FacerARAssetsManager.FacerArAssetCallBack() {
                    @Override
                    public void onResult(String path) {
                        if (!initFlag) {
                            initFlag = !initFlag;
                        }
                        syncUIState(false);
                        mActivity.getARController().loadFaceResource(path);
                        mFaceBottomMenuMgr.updateFilterView(mActivity.getARController().getFaceArSettings());
                        mBeautyBtn.setOnClickListener(FaceUI.this);

                    }
                });
            }
        });
    }

    public void onResume() {
        updateFlashIcon(mIsFlashOff);
    }

    public void onPause() {
        mIsFlashOff = true;
    }

    public void release() {
        isRelease = true;
        mTitleBarUIMgr.release();
    }

    /**
     * 处理屏幕方向变化
     *
     * @param orientation
     */
    public void rotateOrientation(Orientation orientation) {
        RotateViewUtils.requestOrientation(mIconBack, orientation, true);
        RotateViewUtils.requestOrientation(mIconCamera, orientation, true);
        RotateViewUtils.requestOrientation(mIconFlash, orientation, true);
        RotateViewUtils.requestOrientation(mFacetipView, orientation, true);
    }

    /**
     * 是否正在使用后置摄像头
     *
     * @return
     */
    public boolean isBackCamera() {
        return mBackCamera;
    }

    public ARRenderer getARRenderer() {
        return mARRenderer;
    }

    @Override
    public void onClick(View v) {
        if (!FaceARApplication.canClick()) {
            return;
        }
        int viewId = v.getId();
        switch (viewId) {
            case R.id.bdar_id_pic_layout:
                if (mRecordStatus != RecordStatus.NOT_CAPTURING) {
                    return;
                }
                mVideoBtn.setVisibility(View.INVISIBLE);
                File file = new File(CAPTURE_DIR, "pic");
                if (!file.exists()) {
                    file.mkdirs();
                }
                mRecordStatus = RecordStatus.CAPTURING;
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String name = sdf.format(date);

                takePicture(new File(file, name + ".jpg").getAbsolutePath(), mTakePictureCallback);
                break;
            case R.id.bdar_id_video_layout:
                if (mRecordStatus == RecordStatus.NOT_CAPTURING) {
                    mRecordStatus = RecordStatus.STARTING;
                    mPicBtn.setVisibility(View.INVISIBLE);
                    file = new File(CAPTURE_DIR, "video");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    date = new Date();
                    sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    name = sdf.format(date);
                    startRecord(new File(file, name + ".mp4").getAbsolutePath(), 10 * 1000,
                            mMovieRecorderCallback);
                } else if (mRecordStatus == RecordStatus.CAPTURING) {
                    mRecordStatus = RecordStatus.STOPPING;
                    stopRecord();
                }

                break;
            case R.id.bdar_titlebar_back:
                mActivity.onBackPressed();
                break;
            case R.id.bdar_titlebar_camera:
                switchCamera();
                getARRenderer().setRenderJumpFrame(true);
                break;
            case R.id.bdar_titlebar_flash:
                onFlashButtonClick();
                break;
            case R.id.bdar_id_sticker_layout:
                mStickerBtn.setVisibility(View.GONE);
                mBeautyBtn.setVisibility(View.GONE);
                mFaceBottomMenuMgr.showFaceMenu(false);
                break;
            case R.id.bdar_id_beauty_layout:
                mStickerBtn.setVisibility(View.GONE);
                mBeautyBtn.setVisibility(View.GONE);
                mFaceBottomMenuMgr.showFaceMenu(true);
                break;
            default:
                return;
        }
    }

    public void onFaceMenuDismiss() {
        mStickerBtn.setVisibility(View.VISIBLE);
        mBeautyBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 摄像头切换按钮被点击
     */
    public void switchCamera() {
        mIconCamera.setEnabled(false);
        mActivity.getARCameraManager().switchCamera();
    }

    /**
     * 闪光灯按钮被点击
     */
    private void onFlashButtonClick() {
        if (mIsFlashOff) {
            openFlash();
        } else {
            closeFlash();
        }
    }

    /**
     * 打开闪光灯
     */
    private void openFlash() {
        // 打开flash
        mActivity.getARCameraManager().openFlash();
    }

    /**
     * 关闭flash
     */
    public void closeFlash() {
        mActivity.getARCameraManager().closeFlash();
    }

    public void startRecord(String outputFile, long totalTimeMs, MovieRecorderCallback callback) {
        mActivity.getARController().startRecord(outputFile, totalTimeMs, callback);
    }

    public void stopRecord() {
        Toast.makeText(getARActivity(), R.string.bdar_record_completing, Toast.LENGTH_LONG).show();
        mActivity.getARController().stopRecord();
    }

    public void takePicture(String outputFile, TakePictureCallback callback) {
        mActivity.getARController().takePicture(outputFile, callback);
    }

    /**
     * 更新闪光灯Ui显示
     */
    private void updateFlashIcon(final boolean isFlashOff) {

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mIconFlash != null) {
                    if (isFlashOff) {
                        mIconFlash.setImageResource(R.drawable.bdar_drawable_btn_flash_disable_selector);
                    } else {
                        mIconFlash.setImageResource(R.drawable.bdar_drawable_btn_flash_enable_selector);
                    }
                }
            }
        });
    }

    @Override
    public void onCameraSwitch(final boolean result, final boolean rear) {

        if (!result) {
            return;
        }
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIconCamera.setEnabled(true);
                mBackCamera = rear;
                // 前置摄像头不能开启闪光灯
                mIsFlashOff = true;
                updateFlashIcon(true);
                if (mARSwitchCameraCallback != null) {
                    mARSwitchCameraCallback.onCameraSwitch(result, rear);
                }
                if (mActivity != null) {
                    mActivity.getARController().switchCamera(!rear);
                }
            }
        });
    }

    @Override
    public void onFlashClose(boolean result) {
        if (result) {
            updateFlashIcon(true);
            mIsFlashOff = true;
        } else {
            mIsFlashOff = false;
        }

    }

    @Override
    public void onFlashOpen(boolean result) {
        if (result) {
            updateFlashIcon(false);
            mIsFlashOff = false;
        } else {
            mIsFlashOff = true;
        }
    }

    public void setShowRecordState(boolean isRecording) {
        mTitleBarUIMgr.setShowRecordState(isRecording);
    }

    public boolean isReleased() {
        return isRelease;
    }

    public View getRootView() {
        return mRootView;
    }

    public RotateImageView getIconBack() {
        return mIconBack;
    }

    public RotateImageView getIconFlash() {
        return mIconFlash;
    }

    public RotateImageView getIconCamera() {
        return mIconCamera;
    }

    public ARActivity getARActivity() {
        return mActivity;
    }

    public void setARSwitchCameraCallback(ARSwitchCameraCallback callback) {
        mARSwitchCameraCallback = callback;
    }

    @Override
    public void onFilterClicked(FaceRes res) {

        if (res != null) {
            mActivity.getARController().changeFilters(Integer.parseInt(res.resPath));
            FaceArSettings settings = mActivity.getARController().getFaceArSettings();
            mActivity.getARController().adjustFilter(settings.getDefaultFilterValue());
            mFaceBottomMenuMgr.setFilterProcess((int) (settings.getDefaultFilterValue() * 100));
        } else {

            mActivity.getARController().changeFilters(500001);
            mActivity.getARController().adjustFilter(0.3f);
            mFaceBottomMenuMgr.setFilterProcess(0);
        }
    }

    @Override
    public void onMaskClicked(FaceRes res) {

        if (beautyIsLoaded < 1 || !maskCanClick) {
            return;
        }
        syncUIState(false);
        // TODO 调用ArController 中的switchCase
        if (res != null) {
            hasLoadMask = true;
            mActivity.getARController().switchCase(res.resPath);

        } else {
            hasLoadMask = false;
            updateFaceTipView(false);
            mActivity.getARController().clearMask();
        }

    }

    @Override
    public void onBeautyProgressChanged(FaceAr.FaceBeautyType beauty, float value) {
        mActivity.getARController().adjustFaceWithType(beauty, value);
    }

    @Override
    public void onFilterProgressChanged(float value) {
        mActivity.getARController().adjustFilter(value);
    }

    boolean initFlag;

    @Override
    public void onFaceArInited() {

    }

    boolean maskCanClick;
    int beautyIsLoaded = 0; // 累加变量 1代表美颜被加载

    @Override
    public void onStickerLoadingFinished(List<String> triggerList) {

        syncUIState(true);
        // 用于判断美颜功能是否存在
        if ((mActivity.getARController().getFaceArSettings() != null
                     && mActivity.getARController().getFaceArSettings().getBeauties().size() > 0)) {
            beautyIsLoaded++;
        }
        if (triggerList == null || triggerList.isEmpty()) {
            currentTrigger = null;
            return;
        }
        currentTrigger = triggerList.get(triggerList.size() - 1);

    }

    private void syncUIState(final boolean isLoadFinish) {
        getARActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLoadFinish) {
                    if (initFlag) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                    maskCanClick = true;
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    maskCanClick = false;
                }
            }
        });
    }

    @Override
    public void onFrameAvailable(final FrameInfo status) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean trackingSucceeded = status.trackingSucceeded;
                updateFaceTipView(trackingSucceeded);
                if (mActivity.getARController() != null) {
                    mBdarFaceBox.setARController(mActivity.getARController());
                    mBdarFaceBox.findFaceArea(status.trackingPoints, status.trackingSucceeded);
                    if (mFaceBoxUIStateFlag != status.trackingSucceeded) {
                        mFaceBoxUIStateFlag = status.trackingSucceeded;
                        if (mFaceBoxUIStateFlag) {
                            showFaceBox();
                        }
                    }
                }
            }
        });
        printlnFps(status);
        frameCount++;

        // 前300 忽略不计 取301到3300
        if (frameCount >= 300 && index <= 3000) {
            index++;

            fpsCount += status.outputFps;
            //            Log.e("facear", "当前帧数" + index + "frameCount = " + frameCount + ", 单帧耗时 = "
            //                    + (1000 / (fpsCount / index) + "帧率；" + (fpsCount / index)));
        }

    }

    int frameCount = 0;
    double fpsCount = 0;
    int index = 0;

    private void printlnFps(FrameInfo status) {
        if (status == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        //        sb.append("inputFps：" + String.format("%.2f fps", status.inputFps));
        //        sb.append("\n\n");
        sb.append("outputFps：" + String.format("%.2f fps", status.outputFps));
        //        sb.append("\n\n");
        //        double time = status.faceDetectionFps == 0 ? 0 : 1000 / status.faceDetectionFps;
        //        sb.append("faceDetection：" + String.format("%.2f ms", time));
        //        sb.append("\n\n");
        //        time = status.trackingFps == 0 ? 0 : 1000 / status.trackingFps;
        //        sb.append("tracking：" + String.format("%.2f ms", time));
        //        sb.append("\n\n");
        //        time = status.animationFps == 0 ? 0 : 1000 / status.animationFps;
        //        sb.append("animation：" + String.format("%.2f ms", time));
        //        sb.append("\n\n");
        //        time = status.augmentationFps == 0 ? 0 : 1000 / status.augmentationFps;
        //        sb.append("augmentation：" + String.format("%.2f ms", time));
        //        sb.append("\n\n");
        //        time = status.imageConversionFps == 0 ? 0 : 1000 / status.imageConversionFps;
        //        sb.append("imageConversion：" + String.format("%.2f ms", time));
        //        sb.append("\n\n");
        //        time = status.integralImageFps == 0 ? 0 : 1000 / status.integralImageFps;
        //        sb.append("integralImage：" + String.format("%.2f ms", time));

        //        Log.e("facear", sb.toString());
    }

    @Override
    public void onTriggerFired(String triggerName) {
        if (currentTrigger != null && currentTrigger.contains(triggerName)) {
            currentTrigger = null;
        }
    }

    @Override
    public void onFilterAvailable() {
        mActivity.getARController().changeFilters(500001);
        mActivity.getARController().adjustFilter(0.3f);
    }

    private void updateFaceTipView(final boolean trackSuccess) {
        getARActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!hasLoadMask) {
                    mFacetipView.setVisibility(View.INVISIBLE);
                    return;
                }
                if (trackSuccess) {
                    if (TextUtils.isEmpty(currentTrigger)) {
                        mFacetipView.setVisibility(View.INVISIBLE);
                    } else if (currentTrigger.contains("browsraised:start")) {
                        mFacetipView.setVisibility(View.VISIBLE);
                        mFacetipView.setImageDrawable(Res.getDrawable("tip_brow"));
                    } else if (currentTrigger.contains("headdown:start")) {
                        mFacetipView.setVisibility(View.VISIBLE);
                        mFacetipView.setImageDrawable(Res.getDrawable("tip_head_down"));
                    } else if (currentTrigger.contains("mouthopen:start")) {
                        mFacetipView.setVisibility(View.VISIBLE);
                        mFacetipView.setImageDrawable(Res.getDrawable("tip_mouth_open"));
                    } else {
                        mFacetipView.setVisibility(View.INVISIBLE);
                    }
                    return;
                }
                if (!trackSuccess) {
                    mFacetipView.setImageDrawable(Res.getDrawable("bdar_face_track_fail"));
                    mFacetipView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private TakePictureCallback mTakePictureCallback = new TakePictureCallback() {
        @Override
        public void onPictureTake(final boolean result, final String filePath) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordStatus = RecordStatus.NOT_CAPTURING;
                    mVideoBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(getARActivity(), filePath, Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    private MovieRecorderCallback mMovieRecorderCallback = new MovieRecorderCallback() {
        @Override
        public void onRecorderStart(final boolean result) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 状态须为开始中，否则返回
                    if (mRecordStatus != RecordStatus.STARTING) {
                        return;
                    }
                    setShowRecordState(true);
                    if (result) {
                        mRecordStatus = RecordStatus.CAPTURING;
                    } else {
                        mPicBtn.setVisibility(View.VISIBLE);
                        mRecordStatus = RecordStatus.NOT_CAPTURING;
                    }
                }
            });
        }

        @Override
        public void onRecorderProcess(final int process) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean needStop = process >= 100;
                    if (needStop) {
                        mRecordStatus = RecordStatus.STOPPING;
                        stopRecord();
                    }
                }
            });
        }

        @Override
        public void onRecorderComplete(final boolean result, final String outFilePath) {
            UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordStatus = RecordStatus.NOT_CAPTURING;
                    setShowRecordState(false);
                    Toast.makeText(getARActivity(), outFilePath, Toast.LENGTH_LONG).show();
                    mPicBtn.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onRecorderError(int error) {
            mPicBtn.setVisibility(View.VISIBLE);
            mRecordStatus = RecordStatus.NOT_CAPTURING;
        }
    };

    public boolean onBackPressed() {

        return initFlag;
    }

    public void onAuth(boolean isPass) {
        initFlag = !isPass;
    }

    public interface ARSwitchCameraCallback {
        void onCameraSwitch(boolean result, boolean rear);
    }

    private enum RecordStatus {
        STARTING,
        CAPTURING,
        STOPPING,
        NOT_CAPTURING
    }
}
