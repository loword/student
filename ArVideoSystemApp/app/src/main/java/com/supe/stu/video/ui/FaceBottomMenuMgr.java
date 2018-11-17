package com.supe.stu.video.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.ar.face.FaceAr;
import com.baidu.ar.face.FaceArSettings;
import com.baidu.ar.util.FileUtils;
import com.baidu.ar.util.Res;
import com.baidu.ar.util.Utils;
import com.supe.stu.video.FaceUI;
import com.supe.stu.video.utils.DeviceInfoUtils;


/**
 * Created by hanyong on 2018/7/20.
 */

public class FaceBottomMenuMgr implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // 美颜的全部布局
    View mBeautyParent;
    View mBeautyListLayout;
    View mBeautyTypeList;
    View mStickerListLayout;

    // 美型 四个
    SeekBar mWhiteningSeekbar;
    SeekBar mDermabrasionSeekbar;
    SeekBar mBigEyes;
    SeekBar mFaceLift;
    // 滤镜
    SeekBar mFilterSeekbar;

    // 贴纸列表
    GridView mStickerListView;
    // 滤镜列表
    RecyclerView mFilterStyleListView;
    // 底部指示器
    View mFilterTabView;
    View mBeautyTabView;
    View mBeautySegmenting;
    View mFilterSegmenting;

    private FaceStickerAdapter mFaceStickerAdapter;
    private FaceFilterStyleRecyAdapter mFaceFilterStyleAdapter;

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private TextView mBubleView;
    private PopupWindow popupWindow;

    View mFaceLayout;

    FaceUI mFaceUI;
    OnFaceMenuChangeListener listener;

    private int paddingLeft;
    private int paddingBottom;

    public FaceBottomMenuMgr(FaceUI faceUi) {
        mFaceUI = faceUi;
        mFaceLayout = Res.inflate("bdar_face_bottom_menu");
        initBeautyView(mFaceLayout);
        initStickter(mFaceLayout);
        initFileterView(mFaceLayout);
    }

    private void initStickter(View rooView) {
        mStickerListLayout = rooView.findViewById(Res.id("bdar_id_sticker_list_layout"));
        mStickerListView = rooView.findViewById(Res.id("bdar_id_stickter_list"));
    }

    private void initFileterView(View rootView) {
        // 滤镜
        mFilterSeekbar = rootView.findViewById(Res.id("bdar_filter_seekbar"));
    }

    private void initBeautyView(View rootView) {

        // 美白
        mWhiteningSeekbar = rootView.findViewById(Res.id("bdar_id_whitening_seekbar"));
        // 磨皮
        mDermabrasionSeekbar = rootView.findViewById(Res.id("bdar_id_dermabrasion_seekbar"));
        // 大眼
        mBigEyes = rootView.findViewById(Res.id("bdar_id_big_eyes"));
        // 瘦脸
        mFaceLift = rootView.findViewById(Res.id("bdar_id_face_lift"));

        mBeautyParent = rootView.findViewById(Res.id("bdar_beauty_parent"));

        mBeautyListLayout = rootView.findViewById(Res.id("bdar_id_beauty_list_layout"));

        mBeautyTypeList = rootView.findViewById(Res.id("bdar_id_beauty_type_list"));

        mFilterTabView = rootView.findViewById(Res.id("bdar_filter_tab"));
        mFilterTabView.setOnClickListener(this);
        mFilterSegmenting = mFilterTabView.findViewById(Res.id("filter_seg"));

        mBeautyTabView = rootView.findViewById(Res.id("bdar_beauty_tab"));
        mBeautyTabView.setOnClickListener(this);
        mBeautySegmenting = mBeautyTabView.findViewById(Res.id("beauty_seg"));

        mFilterStyleListView = rootView.findViewById(Res.id("bdar_filter_style_list"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mFaceUI.getARActivity());
        //设置布局管理器
        mFilterStyleListView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        //设置Adapter
        mFilterStyleListView.setAdapter(mFaceFilterStyleAdapter);

        //设置分隔线
        //        mFilterStyleListView.addItemDecoration( new DividerGridItemDecoration(this ));
        //设置增加或删除条目的动画
        mFilterStyleListView.setItemAnimator(new DefaultItemAnimator());

        rootView.findViewById(Res.id("dbar_face_done")).setOnClickListener(this);
        rootView.findViewById(Res.id("dbar_sticker_done")).setOnClickListener(this);

        initWindowView(mFaceUI.getARActivity());
    }

    public void setFaceMenuChangeListener(OnFaceMenuChangeListener l) {
        listener = l;
    }

    public void showFaceMenu(boolean isBeauty) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(mFaceLayout,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Utils.dipToPx(mFaceUI.getARActivity(), 300));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setAnimationStyle(Res.getStyle("popwindow_anim_style"));
            popupWindow.setFocusable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mFaceUI.onFaceMenuDismiss();
                }
            });
        }

        mStickerListLayout.setVisibility(isBeauty ? View.GONE : View.VISIBLE);
        mBeautyParent.setVisibility(isBeauty ? View.VISIBLE : View.GONE);

        popupWindow.showAtLocation(mFaceUI.getRootView(), Gravity.BOTTOM, 0, 0);
    }

    public void updateFilterView(FaceArSettings settings) {
        if (settings == null) {
            return;
        }

        if (mFaceFilterStyleAdapter == null) {
            mFaceFilterStyleAdapter = new FaceFilterStyleRecyAdapter(11);
            mFaceFilterStyleAdapter.setOnItemClickListener(new FaceFilterStyleRecyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    FaceRes res = mFaceFilterStyleAdapter.onClickPosition(position);
                    if (listener != null) {
                        listener.onFilterClicked(res);
                    }
                    mFaceFilterStyleAdapter.notifyDataSetChanged();
                }
            });
        }

        mFilterSeekbar.setProgress(30);
        FaceArSettings.Beauty beauty = null;
//        mBigEyes.setProgress((int) (beauty.getDefaultValue() * 100));

//        beauty = settings.getBeautyByName(FaceAr.FaceBeautyType.skin.name());
//        mWhiteningSeekbar.setProgress((int) (beauty.getDefaultValue() * 100));

        beauty = settings.getBeautyByName(FaceAr.FaceBeautyType.thinFace.name());
        mFaceLift.setProgress((int) (beauty.getDefaultValue() * 100));

        beauty = settings.getBeautyByName(FaceAr.FaceBeautyType.whiten.name());
        mDermabrasionSeekbar.setProgress((int) (beauty.getDefaultValue() * 100));

        mFilterSeekbar.setOnSeekBarChangeListener(this);
        mWhiteningSeekbar.setOnSeekBarChangeListener(this);
        mDermabrasionSeekbar.setOnSeekBarChangeListener(this);
        mBigEyes.setOnSeekBarChangeListener(this);
        mFaceLift.setOnSeekBarChangeListener(this);

        mFilterStyleListView.setAdapter(mFaceFilterStyleAdapter);
    }

    public void updateMaskView(String maskPath) {
        if (mFaceStickerAdapter == null) {
            mFaceStickerAdapter = new FaceStickerAdapter(FileUtils.getFlleListAsPath(maskPath));
        }
        mStickerListView.setAdapter(mFaceStickerAdapter);
        mStickerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                FaceRes res = mFaceStickerAdapter.onClickPosition(i);
                if (listener != null) {
                    listener.onMaskClicked(res);
                }
                mFaceStickerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void initWindowView(Context context) {
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        // MIUI禁止了开发者使用TYPE_TOAST，Android 7.1.1 对TYPE_TOAST的使用更严格
        // DeviceInfoUtils.isMIUI() ||
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mBubleView = new TextView(context);
        mBubleView.setTextColor(Color.WHITE);
        if (DeviceInfoUtils.checkDeviceHasNavigationBar(context)) {
            paddingBottom = Utils.dipToPx(context, 50);
        } else {
            paddingBottom = Utils.dipToPx(context, 25);
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == Res.id("bdar_filter_tab")) {
            mBeautyListLayout.setVisibility(View.GONE);
            mBeautyTypeList.setVisibility(View.VISIBLE);
            mBeautySegmenting.setVisibility(View.INVISIBLE);
            mFilterSegmenting.setVisibility(View.VISIBLE);
        } else if (view.getId() == Res.id("bdar_beauty_tab")) {
            mBeautyListLayout.setVisibility(View.VISIBLE);
            mBeautyTypeList.setVisibility(View.GONE);
            mBeautySegmenting.setVisibility(View.VISIBLE);
            mFilterSegmenting.setVisibility(View.INVISIBLE);

        } else if (view.getId() == Res.id("dbar_face_done")
                || view.getId() == Res.id("dbar_sticker_done")) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    }

    public void setFilterProcess(int value) {
        mFilterSeekbar.setProgress(value);
    }

    public interface OnFaceMenuChangeListener {

        void onFilterClicked(FaceRes res);

        void onMaskClicked(FaceRes res);

        void onBeautyProgressChanged(FaceAr.FaceBeautyType beauty, float value);

        void onFilterProgressChanged(float value);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {

        int length = seekBar.getWidth();
        int bubleWidth = mBubleView.getWidth();
        mLayoutParams.x = (int) (paddingLeft + i * length / 100 - bubleWidth / 2);
        try {
            mWindowManager.updateViewLayout(mBubleView, mLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBubleView.setText(i + "%");

        if (listener == null) {
            return;
        }
        if (seekBar == mWhiteningSeekbar) {
            float value = i / 100.f;
            listener.onBeautyProgressChanged(FaceAr.FaceBeautyType.whiten, value);

        }  else if (seekBar == mFaceLift) {
            float value = i / 100.f;
            listener.onBeautyProgressChanged(FaceAr.FaceBeautyType.thinFace, value);
        } else if (seekBar == mFilterSeekbar) {
            float max = 0.8f;
            float scroll = i / 100.f * max;
            // 调节指定id的滤镜参数
            listener.onFilterProgressChanged(scroll);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mBubleView.isAttachedToWindow()) {
            mWindowManager.removeViewImmediate(mBubleView);
            return;
        }
        int process = seekBar.getProgress();
        int length = seekBar.getWidth();

        int[] loc = new int[2];
        seekBar.getLocationOnScreen(loc);
        paddingLeft = loc[0];
        mLayoutParams.x = paddingLeft + process * length / 100;
        mLayoutParams.y = loc[1] - paddingBottom;
        mWindowManager.addView(mBubleView, mLayoutParams);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mBubleView.isAttachedToWindow()) {
            mWindowManager.removeViewImmediate(mBubleView);
        }
    }
}
