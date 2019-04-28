package com.meishe.sdkdemo.particle;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meicam.sdk.NvsAVFileInfo;
import com.meicam.sdk.NvsAssetPackageParticleDescParser;
import com.meicam.sdk.NvsCaptureVideoFx;
import com.meicam.sdk.NvsParticleSystemContext;
import com.meicam.sdk.NvsSize;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsVideoStreamInfo;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BasePermissionActivity;
import com.meishe.sdkdemo.edit.data.FilterItem;
import com.meishe.sdkdemo.edit.filter.FilterAdapter;
import com.meishe.sdkdemo.interfaces.TipsButtonClickListener;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.Constants;
import com.meishe.sdkdemo.utils.MediaScannerUtil;
import com.meishe.sdkdemo.utils.ParameterSettingValues;
import com.meishe.sdkdemo.utils.PathUtils;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.TimeFormatUtil;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.asset.NvAssetManager;
import com.meishe.sdkdemo.utils.dataInfo.ClipInfo;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;
import com.meishe.sdkdemo.utils.permission.PermissionsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleCaptureActivity extends BasePermissionActivity implements NvsStreamingContext.CaptureDeviceCallback,
        NvsStreamingContext.CaptureRecordingDurationCallback,
        NvsStreamingContext.CaptureRecordingStartedCallback {
    private static final String TAG = "ParticleCaptureActivity";

    private final int MIN_RECORD_DURATION = 1000000;
    private static final int TYPE_CAPTURE_FX_CUSTOM = 0;    // 滤镜：一般模式
    private static final int TYPE_CAPTURE_FX_FACE = 1;      // 滤镜：人脸模式
    private static final int TYPE_CAPTURE_FX_HAND = 2;      // 滤镜：手势模式
    private Context mContext;

    private LiveWindow mLiveWindow;
    private NvsStreamingContext mStreamingContext;
    private Button mCloseButton;
    private LinearLayout mFunctionButtonLayout, mSwitchFacingLayout, mFlashLayout, mParticleLayout, mGraffitiLayout;
    private RelativeLayout mStartLayout;
    private ImageView mFlashButton, mStartRecordingImage, mDelete, mNext, mImageAutoFocusRect;
    private TextView mStartText, mRecordTime;

    // 录制相关
    private List<Long> mRecordTimeList = new ArrayList<>();
    private List<String> mRecordFileList = new ArrayList<>(), mAllRequestPermission = new ArrayList<>();
    private long mEachRecodingVideoTime = 0, mAllRecordingTime = 0;
    private String mCurRecordVideoPath;
    private boolean mIsSwitchingCamera = false;
    private int mCurrentDeviceIndex = 1;
    private NvsStreamingContext.CaptureDeviceCapability mCapability = null;
    private AlphaAnimation mFocusAnimation;

    // 粒子相关
    private NvAssetManager mAssetManager;
    private Map<String, FilterItem> mCustomFxEmitter = new HashMap<>();
    private Map<String, FilterItem> mFaceFxEmitter = new HashMap<>();
    private Map<String, FilterItem> mHandFxEmitter = new HashMap<>();
    private Map<String, FilterItem> mTouchFxEmitter = new HashMap<>();
    private View mParticleFxView;
    private SeekBar mParticleSpeedSeekBar, mParticleSizeSeekBar;
    private Button mButtonFxCustom, mButtonFxFace, mButtonFxHand;
    private RecyclerView mParticleRv;
    private FilterAdapter mParticleFxAdapter;
    private int mCurrentFxType = TYPE_CAPTURE_FX_CUSTOM, mCustomSelectPos, mFaceSelectPos, mHandSelectPos;
    private RelativeLayout mTipsMouthLayout, mTipsEyeLayout, mTipsHandLayout;
    private LinearLayout mLayoutFxRange;
    private STMobileDetected m_actionDetected = null;
    private boolean m_initSTMobileSucceeded = false;
    private List<String> m_currentEmitterList;
    private List<String> m_currentEmitterList_0 = new ArrayList<>();
    private List<String> m_currentEmitterList_90 = new ArrayList<>();
    private List<String> m_currentEmitterList_180 = new ArrayList<>();
    private List<String> m_currentEmitterList_270 = new ArrayList<>();
    private NvsAssetPackageParticleDescParser m_particleDescParser;
    private NvsCaptureVideoFx mCurrentCaptureVideoFx;
    private Accelerometer mAccelerometer = null;

    private CountDownTimer m_showTipsTimer = new CountDownTimer(5000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            setParticleTipsVisable(-1);
        }
    };

    @Override
    protected int initRootView() {
        mContext = this;
        mStreamingContext = NvsStreamingContext.getInstance();
        return R.layout.activity_particle_capture;
    }

    @Override
    protected void initViews() {
        mLiveWindow = (LiveWindow) findViewById(R.id.liveWindow);
        mRecordTime = (TextView) findViewById(R.id.recordTime);
        mImageAutoFocusRect = (ImageView) findViewById(R.id.imageAutoFocusRect);
        mDelete = (ImageView) findViewById(R.id.delete);
        mNext = (ImageView) findViewById(R.id.next);
        mStartLayout = (RelativeLayout) findViewById(R.id.startLayout);
        mStartRecordingImage = (ImageView) findViewById(R.id.startRecordingImage);
        mStartText = (TextView) findViewById(R.id.startText);
        mCloseButton = (Button) findViewById(R.id.closeButton);
        mFunctionButtonLayout = (LinearLayout) findViewById(R.id.functionButtonLayout);
        mSwitchFacingLayout = (LinearLayout) findViewById(R.id.switchFacingLayout);
        mFlashLayout = (LinearLayout) findViewById(R.id.flashLayout);
        mFlashLayout.setEnabled(false);
        mFlashButton = (ImageView) findViewById(R.id.flashButton);
        mFlashButton.setImageAlpha(128);
        mParticleLayout = (LinearLayout) findViewById(R.id.particleLayout);
        mGraffitiLayout = (LinearLayout) findViewById(R.id.graffitiLayout);

        // 粒子特效
        mParticleFxView = findViewById(R.id.particle_fx_layout);
        mParticleRv = (RecyclerView) mParticleFxView.findViewById(R.id.recyclerViewFx);
        mButtonFxCustom = (Button) mParticleFxView.findViewById(R.id.btn_capture_fx_custom);
        mButtonFxFace = (Button) mParticleFxView.findViewById(R.id.btn_capture_fx_face);
        mButtonFxHand = (Button) mParticleFxView.findViewById(R.id.btn_capture_fx_hand);
        mParticleSpeedSeekBar = (SeekBar) mParticleFxView.findViewById(R.id.seek_fx_speed);
        mParticleSizeSeekBar = (SeekBar) mParticleFxView.findViewById(R.id.seek_fx_size);
        mLayoutFxRange = (LinearLayout) mParticleFxView.findViewById(R.id.capture_fx_range_layout);
        ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
        params.height = ScreenUtils.dip2px(mContext, 0);
        mLayoutFxRange.setLayoutParams(params);
        mTipsMouthLayout = (RelativeLayout) findViewById(R.id.tipsMouthLayout);
        mTipsEyeLayout = (RelativeLayout) findViewById(R.id.tipsEyeLayout);
        mTipsHandLayout = (RelativeLayout) findViewById(R.id.tipsHandLayout);
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initData() {
        initParticleData();
        initCapture();
        searchAssetData();
        initParticleList();
        initParticleRecyclerView();
    }

    @Override
    protected void initListener() {
        mAccelerometer.setOnRotationListener(new Accelerometer.OnRotationListener() {
            @Override
            public void onRotationChanged(Accelerometer.CLOCKWISE_ANGLE rotation) {
                if (mCurrentCaptureVideoFx == null) {
                    return;
                }
                if (rotation == Accelerometer.CLOCKWISE_ANGLE.Deg0
                        || rotation == Accelerometer.CLOCKWISE_ANGLE.Deg90
                        || rotation == Accelerometer.CLOCKWISE_ANGLE.Deg180
                        || rotation == Accelerometer.CLOCKWISE_ANGLE.Deg270) {
                    if (mCurrentCaptureVideoFx.getAttachment("rotation") != null &&
                            mCurrentCaptureVideoFx.getAttachment("rotation") == rotation) {
                        return;
                    }
                    Log.e("===>", "manage: rotation");
                    mCurrentCaptureVideoFx.setAttachment("rotation", rotation);
                    if (mCurrentFxType == TYPE_CAPTURE_FX_CUSTOM) {
                        enableEmitter(rotation);
                    }
                }
            }
        });

        mLiveWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeCaptureDialogView();

                float rectHalfWidth = mImageAutoFocusRect.getWidth() / 2;
                if (event.getX() - rectHalfWidth >= 0 && event.getX() + rectHalfWidth <= mLiveWindow.getWidth()
                        && event.getY() - rectHalfWidth >= 0 && event.getY() + rectHalfWidth <= mLiveWindow.getHeight()) {
                    mImageAutoFocusRect.setX(event.getX() - rectHalfWidth);
                    mImageAutoFocusRect.setY(event.getY() - rectHalfWidth);
                    RectF rectFrame = new RectF();
                    rectFrame.set(mImageAutoFocusRect.getX(), mImageAutoFocusRect.getY(),
                            mImageAutoFocusRect.getX() + mImageAutoFocusRect.getWidth(),
                            mImageAutoFocusRect.getY() + mImageAutoFocusRect.getHeight());
                    // 启动自动聚焦
                    mImageAutoFocusRect.startAnimation(mFocusAnimation);
                    mStreamingContext.startAutoFocus(new RectF(rectFrame));
                }
                return false;
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSwitchFacingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSwitchingCamera) {
                    return;
                }
                if (mCurrentDeviceIndex == 0) {
                    mCurrentDeviceIndex = 1;
                    mFlashLayout.setEnabled(false);
                    mFlashButton.setImageResource(R.mipmap.icon_flash_off);
                    mFlashButton.setImageAlpha(128);
                } else {
                    mCurrentDeviceIndex = 0;
                    mFlashLayout.setEnabled(true);
                    mFlashButton.setImageResource(R.mipmap.icon_flash_off);
                    mFlashButton.setImageAlpha(255);
                }
                mIsSwitchingCamera = true;
                startCapturePreview(true);
            }
        });

        mFlashLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStreamingContext.isFlashOn()) {
                    mStreamingContext.toggleFlash(false);
                    mFlashButton.setImageResource(R.mipmap.icon_flash_off);
                    mFlashButton.setImageAlpha(255);
                } else {
                    mStreamingContext.toggleFlash(true);
                    mFlashButton.setImageResource(R.mipmap.icon_flash_on);
                    mFlashButton.setImageAlpha(255);
                }
            }
        });

        mStartRecordingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前在录制状态，可停止视频录制
                if (getCurrentEngineState() == mStreamingContext.STREAMING_ENGINE_STATE_CAPTURERECORDING) {
                    stopRecording();
                } else {
                    mCurRecordVideoPath = PathUtils.getParticleRecordPath();
                    if (mCurRecordVideoPath == null)
                        return;
                    mStartRecordingImage.setEnabled(false);

                    mStartRecordingImage.setBackgroundResource(R.mipmap.particle_capture_recording);
                    mEachRecodingVideoTime = 0;
                    //当前未在视频录制状态，则启动视频录制。此处使用带特效的录制方式
                    if (!mStreamingContext.startRecording(mCurRecordVideoPath))
                        return;
                    isInRecording(false);
                    mRecordFileList.add(mCurRecordVideoPath);
                }
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecordTimeList.size() != 0 && mRecordFileList.size() != 0) {
                    mAllRecordingTime -= mRecordTimeList.get(mRecordTimeList.size() - 1);
                    mRecordTimeList.remove(mRecordTimeList.size() - 1);
                    PathUtils.deleteFile(mRecordFileList.get(mRecordFileList.size() - 1));
                    mRecordFileList.remove(mRecordFileList.size() - 1);
                    mRecordTime.setText(TimeFormatUtil.formatUsToString2(mAllRecordingTime));

                    if (mRecordTimeList.size() == 0) {
                        mStartRecordingImage.setBackgroundResource(R.mipmap.particle_default);
                        mStartText.setVisibility(View.GONE);
                        mDelete.setVisibility(View.GONE);
                        mNext.setVisibility(View.GONE);
                        mRecordTime.setVisibility(View.INVISIBLE);
                    } else {
                        mStartText.setText(mRecordTimeList.size() + "");
                        mRecordTime.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*将拍摄的视频传到下一个页面mRecordFileList*/
                ArrayList<ClipInfo> pathList = new ArrayList<>();
                for (int i = 0; i < mRecordFileList.size(); i++) {
                    ClipInfo clipInfo = new ClipInfo();
                    clipInfo.setFilePath(mRecordFileList.get(i));
                    pathList.add(clipInfo);
                }
                NvsAVFileInfo avFileInfo = mStreamingContext.getAVFileInfo(pathList.get(0).getFilePath());
                if (avFileInfo == null)
                    return;
                TimelineData.instance().clear();//数据清空
                NvsSize size = avFileInfo.getVideoStreamDimension(0);
                int rotation = avFileInfo.getVideoStreamRotation(0);
                if (rotation == NvsVideoStreamInfo.VIDEO_ROTATION_90
                        || rotation == NvsVideoStreamInfo.VIDEO_ROTATION_270) {
                    int tmp = size.width;
                    size.width = size.height;
                    size.height = tmp;
                }
                int makeRatio = size.width > size.height ? NvAsset.AspectRatio_16v9 : NvAsset.AspectRatio_9v16;
                TimelineData.instance().setVideoResolution(Util.getVideoEditResolution(makeRatio));
                TimelineData.instance().setMakeRatio(makeRatio);
                TimelineData.instance().setClipInfoData(pathList);
                mNext.setClickable(false);

                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.START_ACTIVITY_FROM_CAPTURE, true);
                AppManager.getInstance().jumpActivity(ParticleCaptureActivity.this, ParticlePreviewActivity.class, bundle);
            }
        });

        mParticleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptureDialogView();
            }
        });

        mGraffitiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_VIDEO_FROM_PARTICLE);
                bundle.putSerializable("fx_touch", (Serializable) mTouchFxEmitter);
                AppManager.getInstance().jumpActivity(ParticleCaptureActivity.this, SelectVideoActivity.class, bundle);
            }
        });

        mButtonFxCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonFxCustom.setTextColor(ContextCompat.getColor(mContext, R.color.cc4a90e2));
                mButtonFxFace.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                mButtonFxHand.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                if (mCustomSelectPos == 0) {
                    ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                    params.height = ScreenUtils.dip2px(mContext, 0);
                    mLayoutFxRange.setLayoutParams(params);
                } else {
                    ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                    params.height = ScreenUtils.dip2px(mContext, 85);
                    mLayoutFxRange.setLayoutParams(params);
                }
                mCurrentFxType = TYPE_CAPTURE_FX_CUSTOM;
                initParticleRecyclerView();
                changeCurrentFxSelected();
            }
        });

        mButtonFxFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonFxFace.setTextColor(ContextCompat.getColor(mContext, R.color.cc4a90e2));
                mButtonFxCustom.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                mButtonFxHand.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                params.height = ScreenUtils.dip2px(mContext, 0);
                mLayoutFxRange.setLayoutParams(params);

                mCurrentFxType = TYPE_CAPTURE_FX_FACE;
                initParticleRecyclerView();
                changeCurrentFxSelected();
            }
        });

        mButtonFxHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonFxHand.setTextColor(ContextCompat.getColor(mContext, R.color.cc4a90e2));
                mButtonFxFace.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                mButtonFxCustom.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
                ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                params.height = ScreenUtils.dip2px(mContext, 0);
                mLayoutFxRange.setLayoutParams(params);

                mCurrentFxType = TYPE_CAPTURE_FX_HAND;
                initParticleRecyclerView();
                changeCurrentFxSelected();
            }
        });

        // 调节粒子速度
        mParticleSpeedSeekBar.setMax(30);
        mParticleSpeedSeekBar.setProgress(10);
        mParticleSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    float fRange = (float) i / 10;
                    if (fRange <= 0.2f)
                        fRange = 0.2f;
                    if (mCurrentCaptureVideoFx == null) {
                        return;
                    }
                    NvsParticleSystemContext particleContext = mCurrentCaptureVideoFx.getParticleSystemContext();
                    if (particleContext == null) {
                        return;
                    }
                    if (m_currentEmitterList == null) {
                        return;
                    }
                    for (int iEmiter = 0; iEmiter < m_currentEmitterList.size(); ++iEmiter) {
                        particleContext.setEmitterRateGain(m_currentEmitterList.get(iEmiter), fRange);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // 调节粒子大小
        mParticleSizeSeekBar.setMax(30);
        mParticleSizeSeekBar.setProgress(10);
        mParticleSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    if (fromUser) {
                        float fRange = (float) i / 10;
                        if (fRange <= 0.2f)
                            fRange = 0.2f;
                        if (mCurrentCaptureVideoFx == null) {
                            return;
                        }
                        NvsParticleSystemContext particleContext = mCurrentCaptureVideoFx.getParticleSystemContext();
                        if (particleContext == null) {
                            return;
                        }
                        if (m_currentEmitterList == null) {
                            return;
                        }
                        for (int iEmiter = 0; iEmiter < m_currentEmitterList.size(); ++iEmiter) {
                            particleContext.setEmitterParticleSizeGain(m_currentEmitterList.get(iEmiter), fRange);
                        }

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void stopRecording() {
        mStreamingContext.stopRecording();
        mStartRecordingImage.setBackgroundResource(R.mipmap.particle_capture_stop);

        mAllRecordingTime += mEachRecodingVideoTime;
        mRecordTimeList.add(mEachRecodingVideoTime);
        mStartText.setText(mRecordTimeList.size() + "");
        isInRecording(true);
    }

    private void initParticleData() {
        mStreamingContext.removeAllCaptureVideoFx();
        mFocusAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFocusAnimation.setDuration(1000);
        mFocusAnimation.setFillAfter(true);
        mAccelerometer = new Accelerometer(getApplicationContext());
    }

    private void searchAssetData() {
        mAssetManager = NvAssetManager.sharedInstance();
        String bundlePath = "particle/fx";
        mAssetManager.searchReservedAssets(NvAsset.ASSET_PARTICLE, bundlePath);
    }

    private void initCapture() {
        if (null == mStreamingContext) {
            return;
        }
        //给Streaming Context设置采集设备回调接口
        mStreamingContext.setCaptureDeviceCallback(this);
        mStreamingContext.setCaptureRecordingDurationCallback(this);
        mStreamingContext.setCaptureRecordingStartedCallback(this);
        if (mStreamingContext.getCaptureDeviceCount() == 0) {
            return;
        }

        // 将采集预览输出连接到LiveWindow控件
        if (!mStreamingContext.connectCapturePreviewWithLiveWindow(mLiveWindow)) {
            Log.e(TAG, "Failed to connect capture preview with livewindow!");
            return;
        }

        //采集设备数量判定
        if (mStreamingContext.getCaptureDeviceCount() > 1) {
            mSwitchFacingLayout.setEnabled(true);
            mCurrentDeviceIndex = 1;
        } else {
            mSwitchFacingLayout.setEnabled(false);
            mCurrentDeviceIndex = 0;
        }

        initSTMobileDetected();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        } else {
            try {
                startCapturePreview(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "startCapturePreviewException: initCapture failed,under 6.0 device may has no access to camera");
                // 拒绝后，所有按钮禁止点击
                noPermissionDialog();
            }
        }
    }

    private boolean startCapturePreview(boolean deviceChanged) {
        // 判断当前引擎状态是否为采集预览状态
        int captureResolutionGrade = ParameterSettingValues.instance().getCaptureResolutionGrade();
        if (deviceChanged || getCurrentEngineState() != NvsStreamingContext.STREAMING_ENGINE_STATE_CAPTUREPREVIEW) {
            int flags = NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_DONT_USE_SYSTEM_RECORDER |
                    NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_CAPTURE_BUDDY_HOST_VIDEO_FRAME
                    | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_STRICT_PREVIEW_VIDEO_SIZE;
            if (m_initSTMobileSucceeded) {
                flags = flags | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_GRAB_CAPTURED_VIDEO_FRAME;
            }
            if (!mStreamingContext.startCapturePreview(mCurrentDeviceIndex,
                    captureResolutionGrade,
                    flags, null)) {
                Log.e(TAG, "Failed to start capture preview!");
                return false;
            }
        }
        return true;
    }

    // 获取当前引擎状态
    private int getCurrentEngineState() {
        return mStreamingContext.getStreamingEngineState();
    }

    private void updateSettingsWithCapability(int deviceIndex) {
        //获取采集设备能力描述对象，设置自动聚焦，曝光补偿，缩放
        mCapability = mStreamingContext.getCaptureDeviceCapability(deviceIndex);
        if (null == mCapability) {
            return;
        }

        //是否支持闪光灯
        if (mCapability.supportFlash) {
            mFlashLayout.setEnabled(true);
        }

        mImageAutoFocusRect.setX((mLiveWindow.getWidth() - mImageAutoFocusRect.getWidth()) / 2);
        mImageAutoFocusRect.setY((mLiveWindow.getHeight() - mImageAutoFocusRect.getHeight()) / 2);
        RectF rectFrame = new RectF();
        rectFrame.set(mImageAutoFocusRect.getX(), mImageAutoFocusRect.getY(),
                mImageAutoFocusRect.getX() + mImageAutoFocusRect.getWidth(),
                mImageAutoFocusRect.getY() + mImageAutoFocusRect.getHeight());
    }

    private void isInRecording(boolean isInRecording) {
        int show;
        if (isInRecording) {
            show = View.VISIBLE;
            mRecordTime.setTextColor(0xffffffff);
        } else {
            mRecordTime.setTextColor(0xffD0021B);
            show = View.INVISIBLE;
        }
        mCloseButton.setVisibility(show);
        mFunctionButtonLayout.setVisibility(show);
        mDelete.setVisibility(show);
        mNext.setVisibility(show);
        mStartText.setVisibility(show);
        if (mRecordTimeList.isEmpty()) {
            mRecordTime.setVisibility(View.INVISIBLE);
        } else {
            mRecordTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCaptureDeviceCapsReady(int captureDeviceIndex) {
        if (captureDeviceIndex != mCurrentDeviceIndex) {
            return;
        }
        updateSettingsWithCapability(captureDeviceIndex);
    }

    @Override
    public void onCaptureDevicePreviewResolutionReady(int i) {

    }

    @Override
    public void onCaptureDevicePreviewStarted(int i) {
        mIsSwitchingCamera = false;
    }

    @Override
    public void onCaptureDeviceError(int i, int i1) {
        Log.e(TAG, "onCaptureDeviceError: initCapture failed,under 6.0 device may has no access to camera");
        //没有权限
        noPermissionDialog();
    }

    @Override
    public void onCaptureDeviceStopped(int i) {

    }

    @Override
    public void onCaptureDeviceAutoFocusComplete(int i, boolean b) {

    }

    @Override
    public void onCaptureRecordingFinished(int i) {
        // 保存到媒体库
        if (mRecordFileList != null && !mRecordFileList.isEmpty()) {
            for (String path : mRecordFileList) {
                if (path == null) {
                    continue;
                }
                if (path.endsWith(".mp4")) {
                    MediaScannerUtil.scanFile(path, "video/mp4");
                } else if (path.endsWith(".jpg")) {
                    MediaScannerUtil.scanFile(path, "image/jpg");
                }
            }
        }
    }

    @Override
    public void onCaptureRecordingError(int i) {

    }

    @Override
    public void onCaptureRecordingDuration(int i, long l) {
        if (l >= MIN_RECORD_DURATION) {
            mStartRecordingImage.setEnabled(true);
        }
        mEachRecodingVideoTime = l;
        mRecordTime.setVisibility(View.VISIBLE);
        mRecordTime.setText(TimeFormatUtil.formatUsToString2(mAllRecordingTime + mEachRecodingVideoTime));
    }

    @Override
    public void onCaptureRecordingStarted(int i) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PermissionsActivity.PERMISSIONS_DENIED:
                String[] permissionsTips = getResources().getStringArray(R.array.permissions_tips);
                Util.showDialog(ParticleCaptureActivity.this, permissionsTips[0], permissionsTips[1], new TipsButtonClickListener() {
                    @Override
                    public void onTipsButtoClick(View view) {
                        AppManager.getInstance().finishActivity();
                    }
                });

                break;
            case PermissionsActivity.PERMISSIONS_GRANTED:
                break;
            default:
                break;
        }
    }

    @Override
    protected List<String> initPermissions() {
        return Util.getAllPermissionsList();
    }

    @Override
    protected void hasPermission() {
        startCapturePreview(false);
    }

    @Override
    protected void nonePermission() {
        Log.d(TAG, "initCapture failed,above 6.0 device may has no access to camera");
        // 拒绝
        noPermissionDialog();
    }

    @Override
    protected void noPromptPermission() {
        Log.d(TAG, "initCapture failed,above 6.0 device may has no access to camera");
        // 禁止提醒
        noPermissionDialog();
    }

    private void noPermissionDialog() {
        String[] permissionsTips = getResources().getStringArray(R.array.permissions_tips);
        Util.showDialog(ParticleCaptureActivity.this, permissionsTips[0], permissionsTips[1], new TipsButtonClickListener() {
            @Override
            public void onTipsButtoClick(View view) {
                AppManager.getInstance().finishActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStreamingContext != null) {
            mStreamingContext.removeAllCaptureVideoFx();
            mStreamingContext.stop();
        }
        if (m_actionDetected != null) {
            m_actionDetected.closeDetected();
            m_actionDetected = null;
        }
        mAccelerometer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNext.setClickable(true);
        mAccelerometer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCapturePreview(false);
            }
        }, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAccelerometer.stop();

        if (getCurrentEngineState() == mStreamingContext.STREAMING_ENGINE_STATE_CAPTURERECORDING) {
            stopRecording();
        }
    }

    /**
     * 显示窗口
     */
    private void showCaptureDialogView() {
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        translate.setDuration(200);//动画时间200毫秒
        translate.setFillAfter(false);//动画出来控件可以点击
        mStartLayout.startAnimation(translate);

        TranslateAnimation translate2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translate2.setDuration(300);//动画时间300毫秒
        translate2.setFillAfter(false);//动画出来控件可以点击
        mParticleFxView.setAnimation(translate2);

        mStartLayout.setVisibility(View.GONE);
        mParticleFxView.setVisibility(View.VISIBLE);

        isShowCaptureButton(false);
    }

    /**
     * 关闭窗口
     */
    private void closeCaptureDialogView() {
        if (mParticleFxView.getVisibility() != View.VISIBLE) {
            return;
        }
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translate.setDuration(300);//动画时间300毫秒
        translate.setFillAfter(false);//动画出来控件可以点击
        mStartLayout.setAnimation(translate);

        TranslateAnimation translate2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        translate2.setDuration(200);//动画时间200毫秒
        translate2.setFillAfter(false);//动画出来控件可以点击
        mParticleFxView.startAnimation(translate2);

        mStartLayout.setVisibility(View.VISIBLE);
        mParticleFxView.setVisibility(View.GONE);

        isShowCaptureButton(true);
    }

    private void isShowCaptureButton(boolean isShow) {
        int show;
        if (isShow) {
            show = View.VISIBLE;
        } else {
            show = View.INVISIBLE;
        }
        if (show == View.VISIBLE) {
            mCloseButton.requestLayout();
            mFunctionButtonLayout.requestLayout();
            mStartLayout.requestLayout();
            mRecordTime.requestLayout();
        }
        mCloseButton.setVisibility(show);
        mFunctionButtonLayout.setVisibility(show);
        mStartLayout.setVisibility(show);
        mRecordTime.setVisibility(show);
    }

    /**
     * 初始化商汤人脸检测
     */
    private void initSTMobileDetected() {
        m_initSTMobileSucceeded = false;
        m_actionDetected = new STMobileDetected();
        m_initSTMobileSucceeded = m_actionDetected.initSTMobileDetected(this, mStreamingContext);
        if (!m_initSTMobileSucceeded) {
            m_actionDetected = null;
            return;
        }
    }

    private void initParticleList() {
        List<NvAsset> filterList = mAssetManager.getUsableAssets(NvAsset.ASSET_PARTICLE, NvAsset.AspectRatio_All, 0);
        if (filterList == null) {
            return;
        }
        boolean update_ret = Util.getBundleFilterInfoFromJson(this, filterList, "particle/info.json");
        if (!update_ret) {
            return;
        }
        for (int i = 0; i < filterList.size(); ++i) {
            NvAsset oneAsset = filterList.get(i);
            if (oneAsset == null || oneAsset.uuid == null || oneAsset.assetDescription == null) {
                continue;
            }
            FilterItem filterItem = new FilterItem();
            if (oneAsset.isReserved()) {
                String coverPath = "file:///android_asset/particle/fx/";
                coverPath += oneAsset.uuid;
                coverPath += ".jpg";
                oneAsset.coverUrl = coverPath;//加载assets/particle/fx文件夹下的图片
            }
            filterItem.setFilterMode(FilterItem.FILTERMODE_PACKAGE);
            filterItem.setFilterName(oneAsset.name);
            filterItem.setPackageId(oneAsset.uuid);
            filterItem.setAssetDescription(oneAsset.assetDescription);
            filterItem.setImageUrl(oneAsset.coverUrl);
            NvsAssetPackageParticleDescParser particleDescParser = new NvsAssetPackageParticleDescParser(oneAsset.assetDescription);
            int particleType = particleDescParser.GetParticleType();
            if (particleType == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_EYE || particleType == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_MOUTH) {
                mFaceFxEmitter.put(oneAsset.uuid, filterItem);
            } else if (particleType == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_GESTURE) {
                mHandFxEmitter.put(oneAsset.uuid, filterItem);
            } else if (particleType == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_NORMAL) {
                mCustomFxEmitter.put(oneAsset.uuid, filterItem);
            } else if (particleType == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_TOUCH) {
                mTouchFxEmitter.put(oneAsset.uuid, filterItem);
            }
        }
    }

    private void initParticleRecyclerView() {
        if (mParticleFxAdapter == null) {
            mParticleFxAdapter = new FilterAdapter(this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            mParticleRv.setLayoutManager(linearLayoutManager);
            mParticleRv.setAdapter(mParticleFxAdapter);

            mParticleFxAdapter.setOnItemClickListener(new FilterAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (mCurrentFxType == TYPE_CAPTURE_FX_CUSTOM) {
                        if (mCustomSelectPos == position) {
                            return;
                        }
                        if (position == 0) {
                            ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                            params.height = ScreenUtils.dip2px(mContext, 0);
                            mLayoutFxRange.setLayoutParams(params);
                        } else {
                            ViewGroup.LayoutParams params = mLayoutFxRange.getLayoutParams();
                            params.height = ScreenUtils.dip2px(mContext, 85);
                            mLayoutFxRange.setLayoutParams(params);
                        }
                        mCustomSelectPos = position;
                    } else if (mCurrentFxType == TYPE_CAPTURE_FX_FACE) {
                        if (mFaceSelectPos == position) {
                            return;
                        }
                        mFaceSelectPos = position;
                    } else if (mCurrentFxType == TYPE_CAPTURE_FX_HAND) {
                        if (mHandSelectPos == position) {
                            return;
                        }
                        mHandSelectPos = position;
                    }
                    if (position == 0) {
                        clearEffect();
                        return;
                    }
                    changeCurrentFxSelected();
                }
            });
        }
        if (mParticleFxAdapter != null) {
            FilterItem filterItem = new FilterItem();
            filterItem.setFilterName("无");
            filterItem.setImageId(R.mipmap.no);

            switch (mCurrentFxType) {
                case TYPE_CAPTURE_FX_CUSTOM:
                    List<FilterItem> customItems = new ArrayList<>(mCustomFxEmitter.values());
                    customItems.add(0, filterItem);
                    mParticleFxAdapter.setFilterDataList(customItems);
                    mParticleFxAdapter.notifyDataSetChanged();
                    mParticleFxAdapter.setSelectPos(mCustomSelectPos);
                    break;
                case TYPE_CAPTURE_FX_FACE:
                    List<FilterItem> faceItems = new ArrayList<>(mFaceFxEmitter.values());
                    faceItems.add(0, filterItem);
                    mParticleFxAdapter.setFilterDataList(faceItems);
                    mParticleFxAdapter.notifyDataSetChanged();
                    mParticleFxAdapter.setSelectPos(mFaceSelectPos);
                    break;
                case TYPE_CAPTURE_FX_HAND:
                    List<FilterItem> handItems = new ArrayList<>(mHandFxEmitter.values());
                    handItems.add(0, filterItem);
                    mParticleFxAdapter.setFilterDataList(handItems);
                    mParticleFxAdapter.notifyDataSetChanged();
                    mParticleFxAdapter.setSelectPos(mHandSelectPos);
                    break;
            }
        }
    }

    private void setParticleTipsVisable(int Type) {
        int moutVis = View.INVISIBLE;
        int eyeVis = View.INVISIBLE;
        int handVis = View.INVISIBLE;
        if (Type == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_MOUTH) {
            moutVis = View.VISIBLE;
        } else if (Type == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_EYE) {
            eyeVis = View.VISIBLE;
        } else if (Type == NvsAssetPackageParticleDescParser.PARTICLE_TYPE_GESTURE) {
            handVis = View.VISIBLE;
        }
        mTipsMouthLayout.setVisibility(moutVis);
        mTipsEyeLayout.setVisibility(eyeVis);
        mTipsHandLayout.setVisibility(handVis);
    }

    private void clearEffect() {
        mStreamingContext.removeAllCaptureVideoFx();
        if (m_actionDetected != null)
            m_actionDetected.setCurrentParticleEffect(null, null);
        setParticleTipsVisable(-1);
    }

    private void changeCurrentFxSelected() {
        FilterItem cur_fx_item = null;
        if (mCurrentFxType == TYPE_CAPTURE_FX_CUSTOM) {
            if (mCustomSelectPos == 0) {
                clearEffect();
                return;
            }
            cur_fx_item = new ArrayList<>(mCustomFxEmitter.values()).get(mCustomSelectPos - 1);
        } else if (mCurrentFxType == TYPE_CAPTURE_FX_FACE) {
            if (mFaceSelectPos == 0) {
                clearEffect();
                return;
            }
            cur_fx_item = new ArrayList<>(mFaceFxEmitter.values()).get(mFaceSelectPos - 1);
        } else if (mCurrentFxType == TYPE_CAPTURE_FX_HAND) {
            if (mHandSelectPos == 0) {
                clearEffect();
                return;
            }
            cur_fx_item = new ArrayList<>(mHandFxEmitter.values()).get(mHandSelectPos - 1);
        }
        if (cur_fx_item == null || cur_fx_item.getPackageId() == null || cur_fx_item.getPackageId().isEmpty()) {
            clearEffect();
            return;
        }
        NvsAssetPackageParticleDescParser emiterListParser = new NvsAssetPackageParticleDescParser(cur_fx_item.getAssetDescription());
        m_particleDescParser = emiterListParser;
        m_currentEmitterList = emiterListParser.GetParticlePartitionEmitter(0);

        // 获取每个角度的emitter
        int partitionCount = m_particleDescParser.GetParticlePartitionCount();
        m_currentEmitterList_0.clear();
        m_currentEmitterList_90.clear();
        m_currentEmitterList_180.clear();
        m_currentEmitterList_270.clear();
        for (int i = 0; i < partitionCount; i++) {
            List<String> lstName = m_particleDescParser.GetParticlePartitionEmitter(i);
            for (int j = 0; j < lstName.size(); j++) {
                String emitterName = lstName.get(j);
                if (emitterName == null) {
                    continue;
                }
                Log.e("===>", "all emitter name: " + emitterName);
                if (emitterName.endsWith("90")) {
                    m_currentEmitterList_90.add(emitterName);
                } else if (emitterName.endsWith("180")) {
                    m_currentEmitterList_180.add(emitterName);
                } else if (emitterName.endsWith("270")) {
                    m_currentEmitterList_270.add(emitterName);
                } else {
                    m_currentEmitterList_0.add(emitterName);
                }
            }
        }
        if (emiterListParser.GetParticleType() < 0) {
            clearEffect();
            return;
        }
        m_showTipsTimer.cancel();
        m_showTipsTimer.start();

        // 移除所有采集视频特效
        mStreamingContext.removeAllCaptureVideoFx();

        // 添加采集粒子特效
        mCurrentCaptureVideoFx = mStreamingContext.appendPackagedCaptureVideoFx(cur_fx_item.getPackageId());
        if (mCurrentCaptureVideoFx == null)
            return;
        mParticleSizeSeekBar.setMax(30);
        mParticleSizeSeekBar.setProgress(10);
        mParticleSpeedSeekBar.setMax(30);
        mParticleSpeedSeekBar.setProgress(10);

        // disable emiter
        NvsParticleSystemContext particleContext = mCurrentCaptureVideoFx.getParticleSystemContext();
        if (particleContext != null && mCurrentFxType != TYPE_CAPTURE_FX_CUSTOM) {
            for (int i = 0; i < partitionCount; i++) {
                List<String> lstName = emiterListParser.GetParticlePartitionEmitter(i);
                for (int n = 0; n < lstName.size(); n++) {
                    particleContext.setEmitterEnabled(lstName.get(n), false);
                }
            }
        }
        if (m_actionDetected != null) {
            m_actionDetected.setCurrentParticleEffect(particleContext, emiterListParser);
            m_actionDetected.setRotateEmitter(mCurrentCaptureVideoFx, m_currentEmitterList_0, m_currentEmitterList_90, m_currentEmitterList_180, m_currentEmitterList_270);
        }
        if (mCurrentFxType == TYPE_CAPTURE_FX_CUSTOM) {
            setParticleTipsVisable(-1);
        }
        if (mCurrentFxType != TYPE_CAPTURE_FX_CUSTOM) {
            setParticleTipsVisable(emiterListParser.GetParticleType());
        }
    }

    private void enableEmitter(Accelerometer.CLOCKWISE_ANGLE orientation) {
        List<String> disableList = new ArrayList<>();
        List<String> enableList = new ArrayList<>();
        if (orientation == Accelerometer.CLOCKWISE_ANGLE.Deg0) {
            enableList.addAll(m_currentEmitterList_0);
            disableList.addAll(m_currentEmitterList_90);
            disableList.addAll(m_currentEmitterList_180);
            disableList.addAll(m_currentEmitterList_270);
        } else if (orientation == Accelerometer.CLOCKWISE_ANGLE.Deg90) {
            enableList.addAll(m_currentEmitterList_90);
            disableList.addAll(m_currentEmitterList_0);
            disableList.addAll(m_currentEmitterList_180);
            disableList.addAll(m_currentEmitterList_270);
        } else if (orientation == Accelerometer.CLOCKWISE_ANGLE.Deg180) {
            enableList.addAll(m_currentEmitterList_180);
            disableList.addAll(m_currentEmitterList_0);
            disableList.addAll(m_currentEmitterList_90);
            disableList.addAll(m_currentEmitterList_270);
        } else if (orientation == Accelerometer.CLOCKWISE_ANGLE.Deg270) {
            enableList.addAll(m_currentEmitterList_270);
            disableList.addAll(m_currentEmitterList_0);
            disableList.addAll(m_currentEmitterList_90);
            disableList.addAll(m_currentEmitterList_180);
        }
        NvsParticleSystemContext particleContext = mCurrentCaptureVideoFx.getParticleSystemContext();
        if (particleContext == null) {
            return;
        }
        for (int i = 0; i < disableList.size(); ++i) {
            String emitterName = disableList.get(i);
            Log.e("===>", "emitter name: " + emitterName);
            particleContext.setEmitterEnabled(emitterName, false);
        }
        for (int i = 0; i < enableList.size(); ++i) {
            String emitterName = enableList.get(i);
            particleContext.setEmitterEnabled(emitterName, true);
        }
    }
}
