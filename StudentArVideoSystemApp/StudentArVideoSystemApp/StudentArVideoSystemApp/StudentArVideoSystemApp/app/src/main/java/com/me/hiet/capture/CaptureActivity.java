package com.me.hiet.capture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.me.hiet.download.AssetDownloadActivity;
import com.me.hiet.interfaces.TipsButtonClickListener;
import com.me.hiet.utils.dataInfo.ClipInfo;
import com.me.hiet.utils.dataInfo.TimelineData;
import com.me.hiet.utils.dataInfo.VideoClipFxInfo;
import com.meicam.sdk.NvsAVFileInfo;
import com.meicam.sdk.NvsCaptureVideoFx;
import com.meicam.sdk.NvsFxDescription;
import com.meicam.sdk.NvsLiveWindow;
import com.meicam.sdk.NvsSize;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsVideoFrameRetriever;
import com.meicam.sdk.NvsVideoStreamInfo;
import com.me.hiet.MSApplication;
import com.meishe.sdkdemo.R;
import com.me.hiet.hite.BasePermissionActivity;
import com.me.hiet.edit.VideoEditActivity;
import com.me.hiet.edit.adapter.SpaceItemDecoration;
import com.me.hiet.edit.data.FilterItem;
import com.me.hiet.utils.AppManager;
import com.me.hiet.utils.AssetFxUtil;
import com.me.hiet.utils.Constants;
import com.me.hiet.utils.Logger;
import com.me.hiet.utils.MediaScannerUtil;
import com.me.hiet.utils.ParameterSettingValues;
import com.me.hiet.utils.PathUtils;
import com.me.hiet.utils.ScreenUtils;
import com.me.hiet.utils.TimeFormatUtil;
import com.me.hiet.utils.ToastUtil;
import com.me.hiet.utils.Util;
import com.me.hiet.utils.asset.NvAsset;
import com.me.hiet.utils.asset.NvAssetManager;
import com.me.hiet.view.FaceUPropView;
import com.me.hiet.view.FilterView;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder.FRAME_OPTION;

public class CaptureActivity extends BasePermissionActivity implements NvsStreamingContext.CaptureDeviceCallback,
        NvsStreamingContext.CaptureRecordingDurationCallback,
        NvsStreamingContext.CaptureRecordingStartedCallback {
    private static final String TAG = "CaptureActivity.this";

    public static final String BEAUTY_SHAPE_EYE_ENLARGING = "Eye Enlarging";
    public static final String BEAUTY_SHAPE_CHECK_THINNING = "Cheek Thinning";
    public static final String BEAUTY_SHAPE_INTENSITY_FORHEAD = "Intensity Forhead";
    public static final String BEAUTY_SHAPE_INTENSITY_CHIN = "Intensity Chin";
    public static final String BEAUTY_SHAPE_INTENSITY_NOSE = "Intensity Nose";
    public static final String BEAUTY_SHAPE_INTENSITY_MOUTH = "Intensity Mouth";

    private static final double NORMAL_VELUE_INTENSITY_FORHEAD = 0.5;
    private static final double NORMAL_VELUE_INTENSITY_CHIN = 0.5;
    private static final double NORMAL_VELUE_INTENSITY_MOUTH = 0.5;

    public static final int CAPTURE_TYPE_ZOOM = 2;
    public static final int CAPTURE_TYPE_EXPOSE = 3;
    private static final int REQUEST_FILTER_LIST_CODE = 110;
    private static final int ARFACE_LIST_REQUES_CODE = 111;
    private final int MIN_RECORD_DURATION = 1000000;

    private NvsLiveWindow mLiveWindow;
    private NvsStreamingContext mStreamingContext;
    private Button mCloseButton;
    private LinearLayout mFunctionButtonLayout;
    private LinearLayout mSwitchFacingLayout;
    private LinearLayout mFlashLayout;
    private ImageView mFlashButton;
    private LinearLayout mZoomLayout;
    private LinearLayout mExposureLayout;
    private LinearLayout mBeautyLayout;
    private LinearLayout mFilterLayout;
    private LinearLayout mFuLayout;
    private RelativeLayout mStartLayout;
    private ImageView mStartRecordingImage;
    private TextView mStartText;
    private ImageView mDelete;
    private ImageView mNext;
    private TextView mRecordTime;
    private TextView mSeekTitle;
    private TextView mSeekProgress;
    private ImageView mImageAutoFocusRect;

    /*拍照or视频*/
    private RelativeLayout mSelectLayout, mPictureLayout;
    private LinearLayout mRecordTypeLayout;
    private View mTypeRightView;
    private Button mTypePictureBtn, mTypeVideoBtn, mPictureCancel, mPictureOk;
    private int mRecordType = Constants.RECORD_TYPE_VIDEO;
    private ImageView mPictureImage;
    private Bitmap mPictureBitmap;

    /*录制*/
    private ArrayList<Long> mRecordTimeList = new ArrayList<>();
    private ArrayList<String> mRecordFileList = new ArrayList<>();
    private long mEachRecodingVideoTime = 0, mEachRecodingImageTime = 4000000;
    private long mAllRecordingTime = 0;
    private String mCurRecordVideoPath;

    private NvAssetManager mAssetManager;

    private int mCurrentDeviceIndex;
    private boolean mIsSwitchingCamera = false;
    NvsStreamingContext.CaptureDeviceCapability mCapability = null;
    private AlphaAnimation mFocusAnimation;

    /*变焦以及曝光dialog*/
    private AlertDialog mCaptureZoomAndExposeDialog;
    private View mZoomView;
    private SeekBar mZoomSeekbar;
    private SeekBar mExposeSeekbar;
    private int mZoomValue;
    private int mMinExpose;
    private int mCaptureType;

    /*美颜Dialog*/
    private AlertDialog mCaptureBeautyDialog;
    private View mBeautyView;
    private Button mBeauty;
    private Button mBeauty_shape;
    private RelativeLayout mBeautySelect;
    private RelativeLayout mBeautyShapeSelect;
    /*美颜*/
    private ImageView mSharpening_iv_close;
    private ImageView mSharpening_iv_open;
    private Switch mBeauty_switch;
    private TextView mBeauty_switch_text;
    private SeekBar mStrength;
    private SeekBar mWhitening;
    private SeekBar mReddening;
    private Boolean mIsBeautyType = true;
    /*美型*/
    private Switch mBeauty_shape_switch;
    private TextView mBeauty_shape_switch_text;
    private SeekBar mLevel;
    private LinearLayout mBeautyShapeResetLayout;
    private ImageView mBeautyShapeResetIcon;
    private TextView mBeautyShapeResetTxt;
    private RecyclerView mBeautyShapeRecyclerView;
    private ShapeAdapter mShapeAdapter;
    private String mCurBeautyShapeId = BEAUTY_SHAPE_CHECK_THINNING;

    private NvsCaptureVideoFx mBeautyFx;//美颜特效
    private double mStrengthValue;
    private double mWhiteningValue;
    private double mReddeningValue;
    private double mLevelValue = 0;

    //滤镜
    private AlertDialog mFilterDialog;
    private FilterView mFilterView;
    private NvsCaptureVideoFx mCurCaptureVideoFx;
    private ArrayList<FilterItem> mFilterDataArrayList = new ArrayList<>();
    private int mFilterSelPos = 0;
    private VideoClipFxInfo mVideoClipFxInfo = new VideoClipFxInfo();

    //道具
    private boolean mCanUseARFace = false; // 人脸特效是否可用的标识.默认不可用
    private NvsCaptureVideoFx mARFaceU;
    private AlertDialog mFaceUPropDialog;
    private FaceUPropView mFaceUPropView;
    private ArrayList<FilterItem> mPropDataArrayList = new ArrayList<>();
    private int mFaceUPropSelPos = 0;
    private String mFaceUPropName = "";


    private boolean m_supportAutoFocus = false; // 是否支持自动聚焦

    private boolean mSharpenDefault = false;
    private SeekBar mDefaultBeauty_sb;
    private ImageView mDefaultBeauty_iv;
    private boolean mDefaultBeautyOpen = false;
    private LinearLayout mStrengthMenu_ll;
    private LinearLayout mWhiteningMenu_ll;
    private LinearLayout mReddeningMenu_ll;
    private int mBeautyIndex = 3;
    // 美颜开关
    private boolean mBeautySwitchIsOpend = false;
    // 美型开关
    private boolean mBeautyShapeSwitchIsOpen = false;
    // 基础滤镜强度
    private double mDefaultBeautyIntensity = 1.0;
    private RelativeLayout mStrength_bg;
    private RelativeLayout mWhitening_bg;
    private RelativeLayout mReddening_bg;
    private TextView mStrength_tv;
    private TextView mWhitening_tv;
    private TextView mReddening_tv;
    private RelativeLayout mBottomLayout;

    // 权限标示
    boolean ifHashAccess = true;

    @Override
    protected int initRootView() {
        mStreamingContext = NvsStreamingContext.getInstance();
        return R.layout.activity_capture;
    }

    @Override
    protected void initViews() {

        //页面主布局\
        mBottomLayout = (RelativeLayout) findViewById(R.id.capture_bottom_rl);
        mFunctionButtonLayout = (LinearLayout) findViewById(R.id.functionButtonLayout);
        mRecordTypeLayout = (LinearLayout) findViewById(R.id.record_type_layout);

        /*变焦以及曝光dialog*/
        LayoutInflater inflater = LayoutInflater.from(this);
        mZoomView = inflater.inflate(R.layout.zoom_view, null);
        mZoomSeekbar = (SeekBar) mZoomView.findViewById(R.id.zoom_seekbar);
        mExposeSeekbar = (SeekBar) mZoomView.findViewById(R.id.expose_seekbar);
        mSeekTitle = (TextView) mZoomView.findViewById(R.id.seekTitle);
        mSeekProgress = (TextView) mZoomView.findViewById(R.id.seekProgress);

        /*美颜dialog*/
        LayoutInflater beautyInflater = LayoutInflater.from(this);
        mBeautyView = beautyInflater.inflate(R.layout.beauty_view, null);
        mBeauty = (Button) mBeautyView.findViewById(R.id.beauty);
        mBeauty_shape = (Button) mBeautyView.findViewById(R.id.beauty_shape);
        mBeautySelect = (RelativeLayout) mBeautyView.findViewById(R.id.beauty_select);
        mBeautyShapeSelect = (RelativeLayout) mBeautyView.findViewById(R.id.beauty_shape_select);


        /*美颜*/
        mBeauty_switch = (Switch) mBeautyView.findViewById(R.id.beauty_switch);
        mBeauty_switch_text = (TextView) mBeautyView.findViewById(R.id.beauty_switch_text);
        mStrength = (SeekBar) mBeautyView.findViewById(R.id.strength);
        mWhitening = (SeekBar) mBeautyView.findViewById(R.id.whitening);
        mReddening = (SeekBar) mBeautyView.findViewById(R.id.reddening);
        mSharpening_iv_close = (ImageView) mBeautyView.findViewById(R.id.sharpening_iv_close); // 锐化关
        mSharpening_iv_close.setEnabled(false);
        mSharpening_iv_open = (ImageView) mBeautyView.findViewById(R.id.sharpening_iv_open); // 锐化开
        mSharpening_iv_open.setEnabled(false);
        mSharpening_iv_open.setVisibility(View.GONE);
        mDefaultBeauty_iv = (ImageView) mBeautyView.findViewById(R.id.default_beauty_iv); // 基础美颜seekbar
        mDefaultBeauty_iv.setEnabled(false);
        mDefaultBeauty_iv.setAlpha(0.4f);
        mDefaultBeauty_sb = (SeekBar) mBeautyView.findViewById(R.id.default_beauty_sb); // 基础美颜seekbar
        /*美颜选项按钮*/
        mStrengthMenu_ll = (LinearLayout) mBeautyView.findViewById(R.id.strength_menu_ll);
        mWhiteningMenu_ll = (LinearLayout) mBeautyView.findViewById(R.id.whitening_menu_ll);
        mReddeningMenu_ll = (LinearLayout) mBeautyView.findViewById(R.id.reddening_menu_ll);
        mStrengthMenu_ll.setAlpha(0.5f);
        mWhiteningMenu_ll.setAlpha(0.5f);
        mReddeningMenu_ll.setAlpha(0.5f);
        mStrengthMenu_ll.setEnabled(false);
        mWhiteningMenu_ll.setEnabled(false);
        mReddeningMenu_ll.setEnabled(false);
        /*美颜按钮样式相关*/
        mStrength_bg = (RelativeLayout) mBeautyView.findViewById(R.id.strength_bg);
        mWhitening_bg = (RelativeLayout) mBeautyView.findViewById(R.id.whitening_bg);
        mReddening_bg = (RelativeLayout) mBeautyView.findViewById(R.id.reddening_bg);
        mStrength_tv = (TextView) mBeautyView.findViewById(R.id.strength_tv);
        mWhitening_tv = (TextView) mBeautyView.findViewById(R.id.whitening_tv);
        mReddening_tv = (TextView) mBeautyView.findViewById(R.id.reddening_tv);

        /*美型*/
        mBeauty_shape_switch = (Switch) mBeautyView.findViewById(R.id.beauty_shape_switch);
        mBeauty_shape_switch_text = (TextView) mBeautyView.findViewById(R.id.beauty_shape_switch_text);
        mLevel = (SeekBar) mBeautyView.findViewById(R.id.level);
        mBeautyShapeResetLayout = (LinearLayout) mBeautyView.findViewById(R.id.beauty_shape_reset_layout);
        mBeautyShapeResetIcon = (ImageView) mBeautyView.findViewById(R.id.beauty_shape_reset_icon);
        mBeautyShapeResetTxt = (TextView) mBeautyView.findViewById(R.id.beauty_shape_reset_txt);
        mBeautyShapeRecyclerView = (RecyclerView) mBeautyView.findViewById(R.id.beauty_shape_item_list);

        mRecordTime = (TextView) findViewById(R.id.recordTime);
        mImageAutoFocusRect = (ImageView) findViewById(R.id.imageAutoFocusRect);
        mDelete = (ImageView) findViewById(R.id.delete);
        mNext = (ImageView) findViewById(R.id.next);
        mStartLayout = (RelativeLayout) findViewById(R.id.startLayout);
        mStartRecordingImage = (ImageView) findViewById(R.id.startRecordingImage);
        mStartText = (TextView) findViewById(R.id.startText);
        mLiveWindow = (NvsLiveWindow) findViewById(R.id.liveWindow);
        mCloseButton = (Button) findViewById(R.id.closeButton);
        mSwitchFacingLayout = (LinearLayout) findViewById(R.id.switchFacingLayout);
        mFlashLayout = (LinearLayout) findViewById(R.id.flashLayout);
        mFlashButton = (ImageView) findViewById(R.id.flashButton);
        mZoomLayout = (LinearLayout) findViewById(R.id.zoomLayout);
        mExposureLayout = (LinearLayout) findViewById(R.id.exposureLayout);
        mBeautyLayout = (LinearLayout) findViewById(R.id.beautyLayout);
        mFilterLayout = (LinearLayout) findViewById(R.id.filterLayout);
        mFuLayout = (LinearLayout) findViewById(R.id.fuLayout);

        /*拍照or视频*/
        mTypeRightView = findViewById(R.id.rightView);
        mTypePictureBtn = (Button) findViewById(R.id.type_picture_btn);
        mTypeVideoBtn = (Button) findViewById(R.id.type_video_btn);
        mSelectLayout = (RelativeLayout) findViewById(R.id.select_layout);
        mPictureLayout = (RelativeLayout) findViewById(R.id.picture_layout);
        mPictureCancel = (Button) findViewById(R.id.picture_cancel);
        mPictureOk = (Button) findViewById(R.id.picture_ok);
        mPictureImage = (ImageView) findViewById(R.id.picture_image);

        mCaptureZoomAndExposeDialog = new AlertDialog.Builder(this).create();
        mCaptureZoomAndExposeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeCaptureDialogView(mCaptureZoomAndExposeDialog);
            }
        });

        mCaptureBeautyDialog = new AlertDialog.Builder(this).create();
        mCaptureBeautyDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeCaptureDialogView(mCaptureBeautyDialog);
            }
        });
        beautyClickListener();
    }

    private void searchAssetData() {
        mAssetManager = NvAssetManager.sharedInstance();
        mAssetManager.searchLocalAssets(NvAsset.ASSET_FILTER);
        String bundlePath = "filter";
        mAssetManager.searchReservedAssets(NvAsset.ASSET_FILTER, bundlePath);

        mAssetManager.searchLocalAssets(NvAsset.ASSET_FACE1_STICKER);
        bundlePath = "arface";
        mAssetManager.searchReservedAssets(NvAsset.ASSET_FACE_BUNDLE_STICKER, bundlePath);
    }

    //滤镜数据初始化
    private void initFilterList() {
        mFilterDataArrayList.clear();
        mFilterDataArrayList = AssetFxUtil.getFilterData(this,
                getLocalData(NvAsset.ASSET_FILTER),
                null,
                true,
                false);
    }

    private void initFilterDialog() {
        mFilterDialog = new AlertDialog.Builder(this).create();
        mFilterDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeCaptureDialogView(mFilterDialog);
            }
        });
        mFilterView = new FilterView(this);
        //先设置滤镜数据
        mFilterView.setFilterArrayList(mFilterDataArrayList);
        mFilterView.initFilterRecyclerView(this);
        mFilterView.setIntensityLayoutVisible(View.INVISIBLE);
        mFilterView.setIntensityTextVisible(View.GONE);
        mFilterView.setFilterListener(new FilterView.OnFilterListener() {
            @Override
            public void onItmeClick(View v, int position) {
                int count = mFilterDataArrayList.size();
                if (position < 0 || position >= count)
                    return;
                if (mFilterSelPos == position)
                    return;
                mFilterSelPos = position;
                removeAllFilterFx();
                mFilterView.setIntensitySeekBarMaxValue(100);
                mFilterView.setIntensitySeekBarProgress(100);
                if (position == 0) {
                    mFilterView.setIntensityLayoutVisible(View.INVISIBLE);
                    mVideoClipFxInfo.setFxMode(VideoClipFxInfo.FXMODE_BUILTIN);
                    mVideoClipFxInfo.setFxId(null);
                    return;
                }
                mFilterView.setIntensityLayoutVisible(View.VISIBLE);
                FilterItem filterItem = mFilterDataArrayList.get(position);
                int filterMode = filterItem.getFilterMode();
                if (filterMode == FilterItem.FILTERMODE_BUILTIN) {
                    String filterName = filterItem.getFilterName();

                    if (!TextUtils.isEmpty(filterName) && filterItem.getIsCartoon()) {
                        mBeauty_switch.setChecked(false);//删除美颜美颜开关关闭,若添加美颜则删除当前美颜

                        mCurCaptureVideoFx = mStreamingContext.appendBuiltinCaptureVideoFx("Cartoon");
                        mCurCaptureVideoFx.setBooleanVal("Stroke Only", filterItem.getStrokenOnly());
                        mCurCaptureVideoFx.setBooleanVal("Grayscale", filterItem.getGrayScale());

                    } else if (!TextUtils.isEmpty(filterName)) {
                        mCurCaptureVideoFx = mStreamingContext.appendBuiltinCaptureVideoFx(filterName);
                    }
                    mVideoClipFxInfo.setFxMode(VideoClipFxInfo.FXMODE_BUILTIN);
                    mVideoClipFxInfo.setFxId(filterName);
                } else {
                    String filterPackageId = filterItem.getPackageId();
                    if (!TextUtils.isEmpty(filterPackageId)) {
                        mCurCaptureVideoFx = mStreamingContext.appendPackagedCaptureVideoFx(filterPackageId);
                    }
                    mVideoClipFxInfo.setFxMode(VideoClipFxInfo.FXMODE_PACKAGE);
                    mVideoClipFxInfo.setFxId(filterPackageId);
                }

                mCurCaptureVideoFx.setFilterIntensity(1.0f);
            }

            @Override
            public void onMoreFilter() {
                Bundle bundle = new Bundle();
                bundle.putInt("titleResId", R.string.moreFilter);
                bundle.putInt("assetType", NvAsset.ASSET_FILTER);
                AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(), AssetDownloadActivity.class, bundle, REQUEST_FILTER_LIST_CODE);
                mFilterView.setMoreFilterClickable(false);
            }

            @Override
            public void onIntensity(int value) {
                if (mCurCaptureVideoFx != null) {
                    float intensity = value / (float) 100;
                    mCurCaptureVideoFx.setFilterIntensity(intensity);
                }
            }
        });
    }

    //道具
    private void initFacUPropDataList() {
        mPropDataArrayList.clear();
        //先初始化道具数据数据
        ArrayList<NvAsset> faceULocalDataList = getLocalData(NvAsset.ASSET_FACE1_STICKER);
        ArrayList<NvAsset> faceUBundleDataList = getLocalData(NvAsset.ASSET_FACE_BUNDLE_STICKER);
        mPropDataArrayList = AssetFxUtil.getFaceUDataList(faceULocalDataList, faceUBundleDataList);
    }

    private void initFacUPropDialog() {
        mFaceUPropDialog = new AlertDialog.Builder(this).create();
        mFaceUPropDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                closeCaptureDialogView(mFaceUPropDialog);
            }
        });
        mFaceUPropView = new FaceUPropView(this);

        //先设置道具数据
        mFaceUPropView.setPropDataArrayList(mPropDataArrayList);
        mFaceUPropView.initPropRecyclerView(this);
        mFaceUPropView.setFaceUPropListener(new FaceUPropView.OnFaceUPropListener() {
            @Override
            public void onItmeClick(View v, int position) {
                int count = mPropDataArrayList.size();
                if (position < 0 || position >= count)
                    return;
                if (mARFaceU == null)
                    return;
                if (mFaceUPropSelPos == position)
                    return;

                mFaceUPropSelPos = position;
                mFaceUPropName = mPropDataArrayList.get(position).getFilterName();
                Log.e("===>", "ooooo: " + mFaceUPropName);
                mARFaceU.setStringVal("Face Ornament", position != 0 ? mFaceUPropName : "");
            }

            @Override
            public void onMoreFaceUProp() {
                mFaceUPropView.setMoreFaceUPropClickable(false);
                Bundle bundle = new Bundle();
                bundle.putInt("titleResId", R.string.moreFaceU);
                bundle.putInt("assetType", NvAsset.ASSET_FACE1_STICKER);
                AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(), AssetDownloadActivity.class, bundle, ARFACE_LIST_REQUES_CODE);
            }
        });
    }

    private void initShapeRecyclerView() {
        mShapeAdapter = new ShapeAdapter(this, getShapeDataList());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBeautyShapeRecyclerView.setLayoutManager(linearLayoutManager);
        mBeautyShapeRecyclerView.setAdapter(mShapeAdapter);
        int space = ScreenUtils.dip2px(this, 8);
        mBeautyShapeRecyclerView.addItemDecoration(new SpaceItemDecoration(space, 0));
        mShapeAdapter.setOnItemClickListener(new ShapeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                hiddenAllSeekBars();
                mLevel.setVisibility(View.VISIBLE);
                mCurBeautyShapeId = getShapeDataList().get(position).shapeId;
                double level = mARFaceU.getFloatVal(mCurBeautyShapeId) * 100;
                mLevel.setProgress((int) level);
            }
        });
    }

    private ArrayList<ShapeDataItem> getShapeDataList() {
        ArrayList<ShapeDataItem> list = new ArrayList<>();

        ShapeDataItem cheek_thinning = new ShapeDataItem();
        cheek_thinning.name = getResources().getString(R.string.cheek_thinning);
        cheek_thinning.resId = R.mipmap.cheek_thinning;
        cheek_thinning.type = "Default";
        cheek_thinning.shapeId = BEAUTY_SHAPE_CHECK_THINNING;
        list.add(cheek_thinning);

        ShapeDataItem eye_enlarging = new ShapeDataItem();
        eye_enlarging.name = getResources().getString(R.string.eye_enlarging);
        eye_enlarging.resId = R.mipmap.eye_enlarging;
        eye_enlarging.type = "Default";
        eye_enlarging.shapeId = BEAUTY_SHAPE_EYE_ENLARGING;
        list.add(eye_enlarging);

        ShapeDataItem intensity_forehead = new ShapeDataItem();
        intensity_forehead.name = getResources().getString(R.string.intensity_forehead);
        intensity_forehead.resId = R.mipmap.intensity_forehead;
        intensity_forehead.type = "Custom";
        intensity_forehead.shapeId = BEAUTY_SHAPE_INTENSITY_FORHEAD;
        list.add(intensity_forehead);

        ShapeDataItem intensity_chin = new ShapeDataItem();
        intensity_chin.name = getResources().getString(R.string.intensity_chin);
        intensity_chin.resId = R.mipmap.intensity_chin;
        intensity_chin.shapeId = BEAUTY_SHAPE_INTENSITY_CHIN;
        intensity_chin.type = "Custom";
        list.add(intensity_chin);

        ShapeDataItem intensity_nose = new ShapeDataItem();
        intensity_nose.name = getResources().getString(R.string.intensity_nose);
        intensity_nose.resId = R.mipmap.intensity_nose;
        intensity_nose.type = "Custom";
        intensity_nose.shapeId = BEAUTY_SHAPE_INTENSITY_NOSE;
        list.add(intensity_nose);

        ShapeDataItem intensity_mouth = new ShapeDataItem();
        intensity_mouth.name = getResources().getString(R.string.intensity_mouth);
        intensity_mouth.resId = R.mipmap.intensity_mouth;
        intensity_mouth.type = "Custom";
        intensity_mouth.shapeId = BEAUTY_SHAPE_INTENSITY_MOUTH;
        list.add(intensity_mouth);

        return list;
    }

    private void beautySeekEnabled(Boolean isEnabled) {
        mStrength.setEnabled(isEnabled);
        mStrength.setClickable(isEnabled);
        mWhitening.setEnabled(isEnabled);
        mWhitening.setClickable(isEnabled);
        mReddening.setEnabled(isEnabled);
        mReddening.setClickable(isEnabled);
    }

    private void beautyShapeSeekEnabled(Boolean isEnabled) {
        mLevel.setEnabled(isEnabled);
        mLevel.setClickable(isEnabled);
        mBeautyShapeResetLayout.setEnabled(isEnabled);
        mBeautyShapeResetLayout.setClickable(isEnabled);
        mShapeAdapter.setEnable(isEnabled);
        if (isEnabled) {
            mBeautyShapeResetIcon.setAlpha(1f);
            mBeautyShapeResetTxt.setTextColor(Color.WHITE);
        } else {
            mBeautyShapeResetIcon.setAlpha(0.5f);
            mBeautyShapeResetTxt.setTextColor(getResources().getColor(R.color.ms_disable_color));
        }
    }

    private void initBeautyData() {
        mStrengthValue = 0;
        mWhiteningValue = 0;
        mReddeningValue = 0;
        NvsFxDescription fxDescription = mStreamingContext.getVideoFxDescription("Beauty");
        List<NvsFxDescription.ParamInfoObject> paramInfo = fxDescription.getAllParamsInfo();
        for (NvsFxDescription.ParamInfoObject param : paramInfo) {
            String paramName = param.getString("paramName");
            if (paramName.equals("Strength")) {
                double maxValue = param.getFloat("floatMaxVal");
                mStrengthValue = param.getFloat("floatDefVal");
                Log.e("mStrengthValue=", mStrengthValue + "");
                mStrength.setMax((int) (maxValue * 100));
                mStrength.setProgress((int) (mStrengthValue * 100));
            } else if (paramName.equals("Whitening")) {
                double maxValue = param.getFloat("floatMaxVal");
                mWhiteningValue = param.getFloat("floatDefVal");
                Log.e("mWhiteningValue=", mWhiteningValue + "");
                mWhitening.setMax((int) (maxValue * 100));
                mWhitening.setProgress((int) (mWhiteningValue * 100));
            } else if (paramName.equals("Reddening")) {
                double maxValue = param.getFloat("floatMaxVal");
                mReddeningValue = param.getFloat("floatDefVal");
                Log.e("mReddeningValue=", mReddeningValue + "");
                mReddening.setMax((int) (maxValue * 100));
                mReddening.setProgress((int) (mReddeningValue * 100));
            }
        }

        //  人脸特效是否可用
        if (mCanUseARFace) {
            mARFaceU = mStreamingContext.appendBuiltinCaptureVideoFx("Face Effect");
            mARFaceU.setStringVal("Beautification Package", "assets:/NvBeautification.asset");
            if (mARFaceU != null) {
                mARFaceU.setMenuVal("Face Type", "");
                mARFaceU.setFloatVal("Face Shape Level", 0);
                resetBeautyShapeDefaultValue();
            }
        }
    }

    private void updateTypeRightView() {
        mTypeRightView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = mTypeRightView.getLayoutParams();
                layoutParams.width = mTypePictureBtn.getWidth();
                mTypeRightView.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initData() {
        updateTypeRightView();
        initCaptureData();
        initCapture();
        searchAssetData();

        initBeautyData();
        //滤镜初始化
        initFilterList();
        initFilterDialog();
        //人脸道具初始化
        initFacUPropDataList();
        initFacUPropDialog();
        //美型初始化
        initShapeRecyclerView();

        mBeauty.setSelected(true);
        beautySeekEnabled(false);
        beautyShapeSeekEnabled(false);
    }

    @Override
    protected void initListener() {
        mLiveWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float rectHalfWidth = mImageAutoFocusRect.getWidth() / 2;
                if (event.getX() - rectHalfWidth >= 0 && event.getX() + rectHalfWidth <= mLiveWindow.getWidth()
                        && event.getY() - rectHalfWidth >= 0 && event.getY() + rectHalfWidth <= mLiveWindow.getHeight()) {
                    mImageAutoFocusRect.setX(event.getX() - rectHalfWidth);
                    mImageAutoFocusRect.setY(event.getY() - rectHalfWidth);
                    RectF rectFrame = new RectF();
                    rectFrame.set(mImageAutoFocusRect.getX(), mImageAutoFocusRect.getY(),
                            mImageAutoFocusRect.getX() + mImageAutoFocusRect.getWidth(),
                            mImageAutoFocusRect.getY() + mImageAutoFocusRect.getHeight());
                    //启动自动聚焦
                    mImageAutoFocusRect.startAnimation(mFocusAnimation);
                    if (m_supportAutoFocus)
                        mStreamingContext.startAutoFocus(new RectF(rectFrame));
                }
                return false;
            }
        });
        /*变焦调节*/
        mZoomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean startTracking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (startTracking) {
                    if (mCaptureType == CAPTURE_TYPE_ZOOM)
                        mStreamingContext.setZoom(progress);//设置缩放比例
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                startTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startTracking = false;
            }
        });
        /*曝光补偿调节*/
        mExposeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCaptureType == CAPTURE_TYPE_EXPOSE) {
                    mStreamingContext.setExposureCompensation(progress + mMinExpose);//设置曝光补偿
                    mSeekProgress.setText(progress + mMinExpose + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*切换摄像头*/
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
        /*闪光灯*/
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
        /*变焦*/
        mZoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureType = CAPTURE_TYPE_ZOOM;
                mSeekTitle.setText(R.string.picture_zoom);
                mSeekProgress.setVisibility(View.INVISIBLE);
                mZoomSeekbar.setVisibility(View.VISIBLE);
                mExposeSeekbar.setVisibility(View.INVISIBLE);
                showCaptureDialogView(mCaptureZoomAndExposeDialog, mZoomView);
            }
        });
        /*曝光补偿*/
        mExposureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureType = CAPTURE_TYPE_EXPOSE;
                mSeekTitle.setText(R.string.exposure_compensation);
                mSeekProgress.setVisibility(View.VISIBLE);
                mSeekProgress.setText(mExposeSeekbar.getProgress() + mMinExpose + "");
                mZoomSeekbar.setVisibility(View.INVISIBLE);
                mExposeSeekbar.setVisibility(View.VISIBLE);
                showCaptureDialogView(mCaptureZoomAndExposeDialog, mZoomView);
            }
        });
        /*美颜*/
        mBeautyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptureDialogView(mCaptureBeautyDialog, mBeautyView);
            }
        });

        mBeautyShapeResetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBeautyShapeDefaultValue();
                mLevel.setProgress(0);
                mShapeAdapter.setSelectPos(Integer.MAX_VALUE);
                hiddenAllSeekBars();
                mCurBeautyShapeId = BEAUTY_SHAPE_CHECK_THINNING;
            }
        });

        /*滤镜*/
        mFilterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptureDialogView(mFilterDialog, mFilterView);
            }
        });
        /*道具*/
        mFuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCanUseARFace) {
                    showCaptureDialogView(mFaceUPropDialog, mFaceUPropView);
                } else {
                    String[] versionName = getResources().getStringArray(R.array.sdk_version_tips);
                    Util.showDialog(CaptureActivity.this, versionName[0], versionName[1]);
                }
            }
        });
        /*开始录制*/
        mStartRecordingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前在录制状态，可停止视频录制
                if (getCurrentEngineState() == mStreamingContext.STREAMING_ENGINE_STATE_CAPTURERECORDING) {
                    stopRecording();
                } else {
                    mCurRecordVideoPath = PathUtils.getRecordVideoPath();
                    if (mCurRecordVideoPath == null)
                        return;
                    mStartRecordingImage.setEnabled(false);

                    // 拍视频or拍照片
                    if (mRecordType == Constants.RECORD_TYPE_VIDEO) {
                        mStartRecordingImage.setBackgroundResource(R.mipmap.particle_capture_recording);
                        mEachRecodingVideoTime = 0;
                        //当前未在视频录制状态，则启动视频录制。此处使用带特效的录制方式
                        if (!mStreamingContext.startRecording(mCurRecordVideoPath))
                            return;
                        isInRecording(false);
                        mRecordFileList.add(mCurRecordVideoPath);
                    } else if (mRecordType == Constants.RECORD_TYPE_PICTURE) {
                        mStreamingContext.startRecording(mCurRecordVideoPath);
                        isInRecording(false);
                    }
                }
            }
        });
        /*删除视频*/
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
        /*下一步，进入编辑*/
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
                AppManager.getInstance().jumpActivity(CaptureActivity.this, VideoEditActivity.class, bundle);
            }
        });

        mTypePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectRecordType(true);
            }
        });

        mTypeRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectRecordType(false);
            }
        });

        mPictureCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurRecordVideoPath != null) {
                    File file = new File(mCurRecordVideoPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                showPictureLayout(false);
                isInRecording(true);
                if (mRecordTimeList.isEmpty()) {
                    mDelete.setVisibility(View.INVISIBLE);
                    mNext.setVisibility(View.INVISIBLE);
                    mStartText.setVisibility(View.INVISIBLE);
                }
            }
        });

        mPictureOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 拍照片
                if (mRecordType == Constants.RECORD_TYPE_PICTURE) {
                    mAllRecordingTime += mEachRecodingImageTime;
                    mRecordTimeList.add(mEachRecodingImageTime);
                    mRecordTime.setText(TimeFormatUtil.formatUsToString2(mAllRecordingTime));
                    mStartText.setText(mRecordTimeList.size() + "");
                    isInRecording(true);
                }
                String jpgPath = PathUtils.getRecordPicturePath();
                boolean save_ret = Util.saveBitmapToSD(mPictureBitmap, jpgPath);
                if (save_ret) {
                    mRecordFileList.add(jpgPath);
                }
                if (mCurRecordVideoPath != null) {
                    File file = new File(mCurRecordVideoPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                showPictureLayout(false);
            }
        });
    }

    /* 美颜dialog 动作监听*/
    private void beautyClickListener() {
        /*美颜*/
        mBeauty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsBeautyType) {
                    hiddenAllSeekBars();
                    if (mBeautySwitchIsOpend && mBeautyIndex >= 0 && mBeautyIndex <= 2) {
                        updateBeautyMenuStyle(mBeautyIndex);
                        // 显示对应seekbar
                        switch (mBeautyIndex) {
                            case 0:
                                mStrength.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                mWhitening.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                mReddening.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                    if (mBeautySwitchIsOpend && mBeautyIndex > 2 && mBeautyFx.getBooleanVal("Default Beauty Enabled")) {
                        mDefaultBeauty_sb.setVisibility(View.VISIBLE);
                    }
                    mIsBeautyType = true;
                    mBeauty.setSelected(true);
                    mBeauty_shape.setSelected(false);
                    mBeautySelect.setVisibility(View.VISIBLE);
                    mBeautyShapeSelect.setVisibility(View.GONE);
                    if (!mSharpenDefault) {
                        mSharpening_iv_close.setVisibility(View.VISIBLE);
                    } else {
                        mSharpening_iv_open.setVisibility(View.VISIBLE);
                    }
                    mDefaultBeauty_iv.setVisibility(View.VISIBLE);
                }
            }
        });
        /*美型*/
        mBeauty_shape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBeautyType) {
                    hiddenAllSeekBars();
                    if (mBeautyShapeSwitchIsOpen && mShapeAdapter.getSelectPos() >= 0 && mShapeAdapter.getSelectPos() <= mShapeAdapter.getItemCount()) {
                        mLevel.setVisibility(View.VISIBLE);
                    }
                    mIsBeautyType = false;
                    mBeauty.setSelected(false);
                    mBeauty_shape.setSelected(true);
                    mBeautySelect.setVisibility(View.GONE);
                    mBeautyShapeSelect.setVisibility(View.VISIBLE);
                    mSharpening_iv_close.setVisibility(View.GONE);
                    mSharpening_iv_open.setVisibility(View.GONE);
                    mDefaultBeauty_iv.setVisibility(View.INVISIBLE);
                }
            }
        });
        /*锐化关状态——点击*/
        mSharpening_iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharpenDefault = true;
                mBeautyFx.setBooleanVal("Default Sharpen Enabled", true);
                mSharpening_iv_close.setVisibility(View.GONE);
                mSharpening_iv_open.setVisibility(View.VISIBLE);
                ToastUtil.showToastCenterNoBg(getApplicationContext(), getResources().getString(R.string.sharpen_open));
            }
        });
        /*锐化开状态——点击*/
        mSharpening_iv_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharpenDefault = false;
                mBeautyFx.setBooleanVal("Default Sharpen Enabled", false);
                mSharpening_iv_close.setVisibility(View.VISIBLE);
                mSharpening_iv_open.setVisibility(View.GONE);
                ToastUtil.showToastCenterNoBg(getApplicationContext(), getResources().getString(R.string.sharpen_close));
            }
        });
        /*基础美颜*/
        mDefaultBeauty_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDefaultBeautyOpen = !mDefaultBeautyOpen;
                if (mBeautyFx.getBooleanVal("Default Beauty Enabled") && (mDefaultBeauty_sb.getVisibility() == View.VISIBLE)) {
                    mBeautyFx.setBooleanVal("Default Beauty Enabled", false);
                    ToastUtil.showToastCenterNoBg(getApplicationContext(), getResources().getString(R.string.default_beauty_close));
                    mDefaultBeauty_iv.setAlpha(0.4f);
                    mDefaultBeauty_sb.setVisibility(View.GONE);
                    hiddenAllSeekBars();
                    if (mBeautySwitchIsOpend && mBeautyIndex >= 0 && mBeautyIndex <= 2) {
                        // 显示对应seekbar
                        switch (mBeautyIndex) {
                            case 0:
                                mStrength.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                mWhitening.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                mReddening.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                    return;
                }
                if (mBeautyFx.getBooleanVal("Default Beauty Enabled") && (mDefaultBeauty_sb.getVisibility() == View.GONE)) {
                    hiddenAllSeekBars();
                    mDefaultBeauty_sb.setVisibility(View.VISIBLE);
                }
                if (!mBeautyFx.getBooleanVal("Default Beauty Enabled") && (mDefaultBeauty_sb.getVisibility() == View.GONE)) {
                    mBeautyFx.setBooleanVal("Default Beauty Enabled", true);
                    ToastUtil.showToastCenterNoBg(getApplicationContext(), getResources().getString(R.string.default_beauty_open));
                    mDefaultBeauty_iv.setAlpha(1.0f);
                    hiddenAllSeekBars();
                    mDefaultBeauty_sb.setVisibility(View.VISIBLE);
                }
            }
        });
        /*美颜*/
        mBeauty_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBeautySwitchIsOpend = isChecked;
                if (isChecked) {
                    mSharpening_iv_close.setEnabled(true);
                    if (mSharpening_iv_close.getVisibility() == View.GONE)
                        mSharpening_iv_close.setVisibility(View.VISIBLE);
                    mSharpening_iv_open.setEnabled(true);
                    mDefaultBeauty_iv.setEnabled(true);
                    mDefaultBeauty_iv.setAlpha(1.0f);
                    mStrengthMenu_ll.setEnabled(true);
                    mWhiteningMenu_ll.setEnabled(true);
                    mReddeningMenu_ll.setEnabled(true);
                    mStrengthMenu_ll.setAlpha(1.0f);
                    mWhiteningMenu_ll.setAlpha(1.0f);
                    mReddeningMenu_ll.setAlpha(1.0f);
                    //添加美颜采集特效
                    mBeautyFx = mStreamingContext.appendBeautyCaptureVideoFx();
                    mBeautyFx.setBooleanVal("Default Sharpen Enabled", mSharpenDefault);
                    mBeautyFx.setFloatVal("Strength", mStrengthValue);//设置美颜强度值
                    mBeautyFx.setFloatVal("Whitening", mWhiteningValue);
                    mBeautyFx.setFloatVal("Reddening", mReddeningValue);
                    // 基础滤镜强度
                    mDefaultBeautyIntensity = mBeautyFx.getFloatVal("Default Intensity");
                    mDefaultBeauty_sb.setProgress((int) (mDefaultBeautyIntensity * 100));
                    boolean ret = removeFilterFxByName("Cartoon");
                    if (ret) {
                        mFilterView.setSelectedPos(0);
                        mFilterView.notifyDataSetChanged();
                    }
                    mBeauty_switch_text.setText(R.string.beauty_close);
                    mDefaultBeauty_sb.setVisibility(View.VISIBLE);
                } else {
                    // 重置索引位置
                    mBeautyIndex = 3;
                    if (mBeauty.isSelected()) {
                        mSharpening_iv_close.setEnabled(false);
                        mSharpening_iv_close.setVisibility(View.VISIBLE);
                        mSharpening_iv_open.setEnabled(false);
                        mSharpening_iv_open.setVisibility(View.GONE);
                        mDefaultBeauty_iv.setEnabled(false);
                    }
                    if (mBeauty_shape.isSelected()) {
                        mSharpening_iv_close.setEnabled(false);
                        mSharpening_iv_close.setVisibility(View.GONE);
                        mSharpening_iv_open.setEnabled(false);
                        mSharpening_iv_open.setVisibility(View.GONE);
                        mDefaultBeauty_iv.setEnabled(false);
                    }
                    updateBeautyMenuStyle(5);
                    mDefaultBeauty_iv.setAlpha(0.4f);
                    mStrengthMenu_ll.setEnabled(false);
                    mWhiteningMenu_ll.setEnabled(false);
                    mReddeningMenu_ll.setEnabled(false);
                    mStrengthMenu_ll.setAlpha(0.5f);
                    mWhiteningMenu_ll.setAlpha(0.5f);
                    mReddeningMenu_ll.setAlpha(0.5f);
                    hiddenBeautySeekBars();
                    mSharpenDefault = false;
                    mBeauty_switch_text.setText(R.string.beauty_open);
                    removeFilterFxByName("Beauty");
                    mBeautyFx = null;
                }
                mBeauty_switch.setChecked(isChecked);
                beautySeekEnabled(isChecked);
            }
        });
        /*基础滤镜seekbar*/
        mDefaultBeauty_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDefaultBeautyIntensity = progress * 1.0 / 100;
                mBeautyFx.setFloatVal("Default Intensity", mDefaultBeautyIntensity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*磨皮按钮*/
        mStrengthMenu_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeautyIndex = 0;
                hiddenAllSeekBars();
                mStrength.setVisibility(View.VISIBLE);
                updateBeautyMenuStyle(0);
            }
        });
        /*磨皮*/
        mStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStrengthValue = progress * 0.01;
                if (mBeautyFx != null) {
                    mBeautyFx.setFloatVal("Strength", mStrengthValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*美白按钮*/
        mWhiteningMenu_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeautyIndex = 1;
                hiddenAllSeekBars();
                mWhitening.setVisibility(View.VISIBLE);
                updateBeautyMenuStyle(1);
            }
        });
        /*美白*/
        mWhitening.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mWhiteningValue = progress * 0.01;
                if (mBeautyFx != null) {
                    mBeautyFx.setFloatVal("Whitening", mWhiteningValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*红润按钮*/
        mReddeningMenu_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeautyIndex = 2;
                hiddenAllSeekBars();
                mReddening.setVisibility(View.VISIBLE);
                updateBeautyMenuStyle(2);
            }
        });
        /*红润*/
        mReddening.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReddeningValue = progress * 0.01;

                if (mBeautyFx != null) {
                    mBeautyFx.setFloatVal("Reddening", mReddeningValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // 美型开关
        mBeauty_shape_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBeautyShapeSwitchIsOpen = isChecked;
                if (!mCanUseARFace) {
                    mBeauty_shape_switch.setChecked(false);
                    String[] versionName = getResources().getStringArray(R.array.sdk_version_tips);
                    Util.showDialog(CaptureActivity.this, versionName[0], versionName[1]);
                } else {
                    if (isChecked) {
                        mARFaceU.setMenuVal("Face Type", "Custom");
                        mARFaceU.setFloatVal("Face Shape Level", 4);
                        mBeauty_shape_switch_text.setText(R.string.beauty_shape_close);
                    } else {
                        hiddenAllSeekBars();
                        mARFaceU.setFloatVal("Face Shape Level", 0);
                        mBeauty_shape_switch_text.setText(R.string.beauty_shape_open);
                        mShapeAdapter.setSelectPos(Integer.MAX_VALUE);
                    }
                    mBeauty_shape_switch.setChecked(isChecked);
                    beautyShapeSeekEnabled(isChecked);
                }
            }
        });

        /*程度*/
        mLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mARFaceU == null) {
                    return;
                }
                mLevelValue = ((float) progress) / ((float) 100);
                Log.e("===>", "level: " + mLevelValue);
                mARFaceU.setFloatVal(mCurBeautyShapeId, mLevelValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateBeautyMenuStyle(int beautyMenuIndex) {
        mStrength_bg.setBackgroundResource(0);
        mWhitening_bg.setBackgroundResource(0);
        mReddening_bg.setBackgroundResource(0);
        switch (beautyMenuIndex) {
            case 0:
                mStrength_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_selected);
                mWhitening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mReddening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mStrength_tv.setTextColor(Color.parseColor("#4A90E2"));
                mWhitening_tv.setTextColor(Color.WHITE);
                mReddening_tv.setTextColor(Color.WHITE);
                break;
            case 1:
                mStrength_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mWhitening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_selected);
                mReddening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mStrength_tv.setTextColor(Color.WHITE);
                mWhitening_tv.setTextColor(Color.parseColor("#4A90E2"));
                mReddening_tv.setTextColor(Color.WHITE);
                break;
            case 2:
                mStrength_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mWhitening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mReddening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_selected);
                mStrength_tv.setTextColor(Color.WHITE);
                mWhitening_tv.setTextColor(Color.WHITE);
                mReddening_tv.setTextColor(Color.parseColor("#4A90E2"));
                break;
            default:
                mStrength_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mWhitening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mReddening_bg.setBackgroundResource(R.drawable.beauty_shape_border_shape_default);
                mStrength_tv.setTextColor(Color.WHITE);
                mWhitening_tv.setTextColor(Color.WHITE);
                mReddening_tv.setTextColor(Color.WHITE);
                break;
        }
    }

    private void hiddenBeautySeekBars() {
        if (mDefaultBeauty_sb.getVisibility() == View.VISIBLE) {
            mDefaultBeauty_sb.setVisibility(View.GONE);
        }
        if (mStrength.getVisibility() == View.VISIBLE) {
            mStrength.setVisibility(View.GONE);
        }
        if (mWhitening.getVisibility() == View.VISIBLE) {
            mWhitening.setVisibility(View.GONE);
        }
        if (mReddening.getVisibility() == View.VISIBLE) {
            mReddening.setVisibility(View.GONE);
        }
    }

    private void hiddenAllSeekBars() {
        if (mDefaultBeauty_sb.getVisibility() == View.VISIBLE) {
            mDefaultBeauty_sb.setVisibility(View.GONE);
        }
        if (mStrength.getVisibility() == View.VISIBLE) {
            mStrength.setVisibility(View.GONE);
        }
        if (mWhitening.getVisibility() == View.VISIBLE) {
            mWhitening.setVisibility(View.GONE);
        }
        if (mReddening.getVisibility() == View.VISIBLE) {
            mReddening.setVisibility(View.GONE);
        }
        if (mLevel.getVisibility() == View.VISIBLE) {
            mLevel.setVisibility(View.GONE);
        }
    }

    private void resetBeautyShapeDefaultValue() {
        mARFaceU.setFloatVal(BEAUTY_SHAPE_EYE_ENLARGING, 0f);
        mARFaceU.setFloatVal(BEAUTY_SHAPE_CHECK_THINNING, 0f);
        mARFaceU.setFloatVal(BEAUTY_SHAPE_INTENSITY_FORHEAD, NORMAL_VELUE_INTENSITY_FORHEAD);
        mARFaceU.setFloatVal(BEAUTY_SHAPE_INTENSITY_CHIN, NORMAL_VELUE_INTENSITY_CHIN);
        mARFaceU.setFloatVal(BEAUTY_SHAPE_INTENSITY_NOSE, 0f);
        mARFaceU.setFloatVal(BEAUTY_SHAPE_INTENSITY_MOUTH, NORMAL_VELUE_INTENSITY_MOUTH);
    }

    private void stopRecording() {
        mStreamingContext.stopRecording();
        mStartRecordingImage.setBackgroundResource(R.mipmap.capture_recording_stop);

        // 拍视频
        if (mRecordType == Constants.RECORD_TYPE_VIDEO) {
            mAllRecordingTime += mEachRecodingVideoTime;
            mRecordTimeList.add(mEachRecodingVideoTime);
            mStartText.setText(mRecordTimeList.size() + "");
            isInRecording(true);
        }
    }

    private void removeAllFilterFx() {
        List<Integer> remove_list = new ArrayList<>();
        for (int i = 0; i < mStreamingContext.getCaptureVideoFxCount(); i++) {
            NvsCaptureVideoFx fx = mStreamingContext.getCaptureVideoFxByIndex(i);
            if (fx == null)
                continue;

            String name = fx.getBuiltinCaptureVideoFxName();
            if (name != null && !name.equals("Beauty") && !name.equals("Face Effect")) {
                remove_list.add(i);
                Log.e("===>", "fx name: " + name);
            }
        }
        if (!remove_list.isEmpty()) {
            for (int i = 0; i < remove_list.size(); i++) {
                mStreamingContext.removeCaptureVideoFx(remove_list.get(i));
            }
        }
    }

    private boolean removeFilterFxByName(String name) {
        for (int i = 0; i < mStreamingContext.getCaptureVideoFxCount(); i++) {
            NvsCaptureVideoFx fx = mStreamingContext.getCaptureVideoFxByIndex(i);
            if (fx.getDescription().getName().equals(name)) {
                mStreamingContext.removeCaptureVideoFx(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 显示窗口
     */
    private void showCaptureDialogView(AlertDialog dialog, View view) {
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

        translate.setDuration(200);//动画时间500毫秒
        translate.setFillAfter(false);//动画出来控件可以点击
        mStartLayout.startAnimation(translate);
        dialog.show();
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        params.dimAmount = 0.0f;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.colorTranslucent));
        dialog.getWindow().setWindowAnimations(R.style.fx_dlg_style);
        isShowCaptureButton(false);
    }

    /**
     * 关闭窗口
     */
    private void closeCaptureDialogView(AlertDialog dialog) {
        dialog.dismiss();
        TranslateAnimation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translate.setDuration(300);//动画时间300毫秒
        translate.setFillAfter(false);//动画出来控件可以点击
        mStartLayout.setAnimation(translate);
        isShowCaptureButton(true);
    }

    private void initCaptureData() {
        mStreamingContext.removeAllCaptureVideoFx();
        mFocusAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFocusAnimation.setDuration(1000);
        mFocusAnimation.setFillAfter(true);
        mCanUseARFace = MSApplication.isCanUseARFace();
    }

    private void initCapture() {
        if (null == mStreamingContext) {
            return;
        }
        //给Streaming Context设置采集设备回调接口
        setStreamingCallback(false);
        if (mStreamingContext.getCaptureDeviceCount() == 0) {
            return;
        }

        // 将采集预览输出连接到LiveWindow控件
        if (!mStreamingContext.connectCapturePreviewWithLiveWindow(mLiveWindow)) {
            Log.e(TAG, "Failed to connect capture preview with livewindow!");
            return;
        }

        mCurrentDeviceIndex = 0;
        //采集设备数量判定
        if (mStreamingContext.getCaptureDeviceCount() > 1) {
            mSwitchFacingLayout.setEnabled(true);
        } else {
            mSwitchFacingLayout.setEnabled(false);
        }
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
                setCaptureViewEnable(false);
            }
        }
        setCaptureViewEnable(true);
    }

    private boolean startCapturePreview(boolean deviceChanged) {
        // 判断当前引擎状态是否为采集预览状态
        int captureResolutionGrade = ParameterSettingValues.instance().getCaptureResolutionGrade();
        if (deviceChanged || getCurrentEngineState() != NvsStreamingContext.STREAMING_ENGINE_STATE_CAPTUREPREVIEW) {
            m_supportAutoFocus = false;
            if (!mStreamingContext.startCapturePreview(mCurrentDeviceIndex, captureResolutionGrade, NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_DONT_USE_SYSTEM_RECORDER | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_CAPTURE_BUDDY_HOST_VIDEO_FRAME | NvsStreamingContext.STREAMING_ENGINE_CAPTURE_FLAG_STRICT_PREVIEW_VIDEO_SIZE, null)) {
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

        m_supportAutoFocus = mCapability.supportAutoFocus;

        // 是否支持缩放
        if (mCapability.supportZoom) {
            mZoomValue = mCapability.maxZoom;
            mZoomSeekbar.setMax(mZoomValue);
            mZoomSeekbar.setProgress(mStreamingContext.getZoom());
            mZoomSeekbar.setEnabled(true);
        } else {
            Log.e(TAG, "该设备不支持缩放");
        }

        // 是否支持曝光补偿
        if (mCapability.supportExposureCompensation) {
            mMinExpose = mCapability.minExposureCompensation;
            mExposeSeekbar.setMax(mCapability.maxExposureCompensation - mMinExpose);
            mExposeSeekbar.setProgress(mStreamingContext.getExposureCompensation() - mMinExpose);
            mExposeSeekbar.setEnabled(true);
        }
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
        mSelectLayout.setVisibility(show);
        if (mRecordTimeList.isEmpty()) {
            mRecordTime.setVisibility(View.INVISIBLE);
        } else {
            mRecordTime.setVisibility(View.VISIBLE);
        }
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

    private ArrayList<NvAsset> getLocalData(int assetType) {
        return mAssetManager.getUsableAssets(assetType, NvAsset.AspectRatio_All, 0);
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
        //没有权限之后，所有按钮禁止点击
        noPermissionDialog();
        setCaptureViewEnable(false);
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
        // 拍视频or拍照片
        if (mRecordType == Constants.RECORD_TYPE_VIDEO) {
            if (l >= MIN_RECORD_DURATION) {
                mStartRecordingImage.setEnabled(true);
            }
            mEachRecodingVideoTime = l;
            mRecordTime.setVisibility(View.VISIBLE);
            mRecordTime.setText(TimeFormatUtil.formatUsToString2(mAllRecordingTime + mEachRecodingVideoTime));
        } else if (mRecordType == Constants.RECORD_TYPE_PICTURE) {
            if (l > 40000) {
                stopRecording();
                takePhoto(l);
            }
        }
    }

    @Override
    public void onCaptureRecordingStarted(int i) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_FILTER_LIST_CODE) {
                    initFilterList();
                    mFilterView.setFilterArrayList(mFilterDataArrayList);
                    mFilterSelPos = AssetFxUtil.getSelectedFilterPos(mFilterDataArrayList, mVideoClipFxInfo);
                    mFilterView.setSelectedPos(mFilterSelPos);
                    mFilterView.notifyDataSetChanged();
                } else if (requestCode == ARFACE_LIST_REQUES_CODE) {
                    initFacUPropDataList();
                    mFaceUPropView.setPropDataArrayList(mPropDataArrayList);
                    mFaceUPropSelPos = AssetFxUtil.getSelectedFaceUPropPos(mPropDataArrayList, mFaceUPropName);
                    mFaceUPropView.setSelectedPos(mFaceUPropSelPos);
                    mFaceUPropView.notifyDataSetChanged();
                }
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
        //初始化拍摄
        startCapturePreview(false);
    }

    @Override
    protected void nonePermission() {
        Log.d(TAG, "initCapture failed,above 6.0 device may has no access to camera");
        // 拒绝后，所有按钮禁止点击
        setCaptureViewEnable(false);
        noPermissionDialog();
    }

    @Override
    protected void noPromptPermission() {
        // 拒绝了权限
        Logger.e(TAG, "initCapture failed,above 6.0 device has no access from user");
        setCaptureViewEnable(false);
        noPermissionDialog();
    }

    private void noPermissionDialog() {
        String[] permissionsTips = getResources().getStringArray(R.array.permissions_tips);
        Util.showDialog(CaptureActivity.this, permissionsTips[0], permissionsTips[1], new TipsButtonClickListener() {
            @Override
            public void onTipsButtoClick(View view) {
                AppManager.getInstance().finishActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNext.setClickable(true);
        mFilterView.setMoreFilterClickable(true);
        mFaceUPropView.setMoreFaceUPropClickable(true);
        startCapturePreview(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getCurrentEngineState() == mStreamingContext.STREAMING_ENGINE_STATE_CAPTURERECORDING) {
            stopRecording();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void destroy() {
        if (mStreamingContext != null) {
            mStreamingContext.removeAllCaptureVideoFx();
            mStreamingContext.stop();
            setStreamingCallback(true);
            mStreamingContext = null;
        }
        mRecordTimeList.clear();
        mRecordFileList.clear();
        mFilterDataArrayList.clear();
        mPropDataArrayList.clear();
    }

    private void setStreamingCallback(boolean isDestroyCallback) {
        mStreamingContext.setCaptureDeviceCallback(isDestroyCallback ? null : this);
        mStreamingContext.setCaptureRecordingDurationCallback(isDestroyCallback ? null : this);
        mStreamingContext.setCaptureRecordingStartedCallback(isDestroyCallback ? null : this);
    }

    private void takePhoto(long time) {
        if (mCurRecordVideoPath != null) {
            NvsVideoFrameRetriever videoFrameRetriever = mStreamingContext.createVideoFrameRetriever(mCurRecordVideoPath);
            if (videoFrameRetriever != null) {
                mPictureBitmap = videoFrameRetriever.getFrameAtTimeWithCustomVideoFrameHeight(time, ScreenUtils.getScreenHeight(this));
                Log.e("===>", "screen: " + ScreenUtils.getScreenWidth(this) + " " + ScreenUtils.getScreenHeight(this));
                Log.e("===>", "picture: " + mPictureBitmap.getWidth() + " " + mPictureBitmap.getHeight());
                if (mPictureBitmap != null) {
                    mPictureImage.setImageBitmap(mPictureBitmap);
                    showPictureLayout(true);
                }
            }
        }
    }

    private void takePhoto2(long time) {
        RequestOptions requestOptions = RequestOptions.frameOf(time);
        requestOptions.set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST);
        requestOptions.transform(new BitmapTransformation() {
            @Override
            protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                Log.e("===>", "screen: " + ScreenUtils.getScreenWidth(CaptureActivity.this) + " " + ScreenUtils.getScreenHeight(CaptureActivity.this));
                Log.e("===>", "glide: " + outWidth + " " + outHeight + " " + toTransform.getWidth() + " " + toTransform.getHeight());
                mPictureBitmap = toTransform;
                return toTransform;
            }

            @Override
            public void updateDiskCacheKey(MessageDigest messageDigest) {
                try {
                    messageDigest.update((getPackageName() + "RotateTransform").getBytes("utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Glide.with(this).load(mCurRecordVideoPath).apply(requestOptions).into(mPictureImage);
        showPictureLayout(true);
    }

    private void selectRecordType(boolean left_to_right) {
        TranslateAnimation ani;
        if (left_to_right) {
            if (mRecordType == Constants.RECORD_TYPE_PICTURE) {
                return;
            }
            ani = new TranslateAnimation(mTypePictureBtn.getX(), mTypeVideoBtn.getX(), 0, 0);
            mTypePictureBtn.setTextColor(ContextCompat.getColor(CaptureActivity.this, R.color.ms_red));
            mTypeVideoBtn.setTextColor(ContextCompat.getColor(CaptureActivity.this, R.color.white));
            mRecordType = Constants.RECORD_TYPE_PICTURE;
        } else {
            ani = new TranslateAnimation(mTypeVideoBtn.getX(), mTypePictureBtn.getX(), 0, 0);
            mTypePictureBtn.setTextColor(ContextCompat.getColor(CaptureActivity.this, R.color.white));
            mTypeVideoBtn.setTextColor(ContextCompat.getColor(CaptureActivity.this, R.color.ms_red));
            mRecordType = Constants.RECORD_TYPE_VIDEO;
        }
        ani.setDuration(300);
        ani.setFillAfter(true);
        mRecordTypeLayout.startAnimation(ani);
    }

    private void showPictureLayout(boolean show) {
        TranslateAnimation topTranslate;
        if (show) {
            mPictureLayout.setVisibility(View.INVISIBLE);
            topTranslate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            topTranslate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPictureLayout.clearAnimation();
                    mCloseButton.setVisibility(View.GONE);
                    mPictureLayout.setVisibility(View.VISIBLE);
                    mPictureLayout.setClickable(true);
                    mPictureLayout.setFocusable(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            mStartRecordingImage.setEnabled(true);
            mCloseButton.setVisibility(View.VISIBLE);
            topTranslate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

            topTranslate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPictureLayout.clearAnimation();
                    mPictureLayout.setVisibility(View.GONE);
                    mPictureLayout.setClickable(false);
                    mPictureLayout.setFocusable(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        topTranslate.setDuration(300);
        topTranslate.setFillAfter(true);
        mPictureLayout.setAnimation(topTranslate);
    }

    public void setCaptureViewEnable(boolean enable) {
        mBottomLayout.setEnabled(enable);
        mBottomLayout.setClickable(enable);
        mFunctionButtonLayout.setEnabled(enable);
        mFunctionButtonLayout.setClickable(enable);
        mRecordTypeLayout.setEnabled(enable);
        mRecordTypeLayout.setClickable(enable);
    }

    static public class ShapeDataItem {
        String shapeId;
        int resId;
        String name;
        String type;
    }
}
