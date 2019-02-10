package com.meishe.sdkdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meicam.sdk.NvsStreamingContext;
import com.meishe.sdkdemo.base.BaseFragmentPagerAdapter;
import com.meishe.sdkdemo.base.BasePermissionActivity;
import com.meishe.sdkdemo.boomrang.BoomRangActivity;
import com.meishe.sdkdemo.capture.CaptureActivity;
import com.meishe.sdkdemo.capturescene.CaptureSceneActivity;
import com.meishe.sdkdemo.douyin.DouYinCaptureActivity;
import com.meishe.sdkdemo.edit.data.BackupData;
import com.meishe.sdkdemo.edit.watermark.SingleClickActivity;
import com.meishe.sdkdemo.feedback.FeedBackActivity;
import com.meishe.sdkdemo.main.MainViewPagerFragment;
import com.meishe.sdkdemo.main.MainViewPagerFragmentData;
import com.meishe.sdkdemo.main.OnItemClickListener;
import com.meishe.sdkdemo.musicLyrics.MultiVideoSelectActivity;
import com.meishe.sdkdemo.particle.ParticleCaptureActivity;
import com.meishe.sdkdemo.selectmedia.SelectMediaActivity;
import com.meishe.sdkdemo.superzoom.SuperZoomActivity;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.Constants;
import com.meishe.sdkdemo.utils.ParameterSettingValues;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.SpUtil;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BasePermissionActivity implements OnItemClickListener {
    public static final int REQUEST_CAMERA_PERMISSION_CODE = 200;
    private ImageView mIvSetting;
    private ImageView mIvFeedBack;
    private RelativeLayout layoutVideoCapture;
    private RelativeLayout layoutVideoEdit;
    private ViewPager mainViewPager;
    private RadioGroup radioGroup;
    private TextView mainVersionNumber;
    private int spanCount = 4;
    private View clickedView = null;

    @Override
    protected int initRootView() {
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return R.layout.activity_main;
        }
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initViews() {
        mIvSetting = (ImageView) findViewById(R.id.iv_main_setting);
        mIvFeedBack = (ImageView) findViewById(R.id.iv_main_feedback);
        layoutVideoCapture = (RelativeLayout) findViewById(R.id.layout_video_capture);
        layoutVideoEdit = (RelativeLayout) findViewById(R.id.layout_video_edit);
        mainViewPager = (ViewPager) findViewById(R.id.main_viewPager);
        radioGroup = (RadioGroup) findViewById(R.id.main_radioGroup);
        mainVersionNumber = (TextView) findViewById(R.id.main_versionNumber);
    }

    @Override
    protected void initData() {
        ParameterSettingValues parameterValues = (ParameterSettingValues) SpUtil.getObjectFromShare(getApplicationContext(), Constants.KEY_PARAMTER);
        if (parameterValues != null) {  // 本地没有存储设置的参数，设置默认值
            ParameterSettingValues.setParameterValues(parameterValues);
        }

        initFragmentAndView();
        String sdkVersionNum = "1.0.0";
        mainVersionNumber.setText(String.format(getResources().getString(R.string.versionNumber), sdkVersionNum));
    }

    @SuppressLint("RestrictedApi")
    private void initFragmentAndView() {
        Map<Integer, List<String>> map = subListByItemCount();
        List<Fragment> mFragmentList;
        mFragmentList = getSupportFragmentManager().getFragments();
        if (mFragmentList == null || mFragmentList.size() == 0) {
            mFragmentList = new ArrayList<>();
            for (int i = 0; i < map.size(); i++) {
                List<String> nameList = map.get(i);
                MainViewPagerFragment mediaFragment = new MainViewPagerFragment();
                Bundle bundle = new Bundle();
                ArrayList<MainViewPagerFragmentData> list = initFragmentDataById(nameList, i + 1);
                bundle.putParcelableArrayList("list", list);
                bundle.putInt("span", spanCount);
                mediaFragment.setArguments(bundle);
                mFragmentList.add(mediaFragment);
            }
        }
        for (int i = 0; i < map.size(); i++) {
            addRadioButton(i);
        }
        BaseFragmentPagerAdapter fragmentPagerAdapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList, null);
        mainViewPager.setAdapter(fragmentPagerAdapter);
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setRadioButtonState(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private Map<Integer, List<String>> subListByItemCount() {
        String[] fragmentItems = getResources().getStringArray(R.array.main_fragment_item);
        Map<Integer, List<String>> map = new HashMap<>();
        List<String> list = Arrays.asList(fragmentItems);
        int count = list.size() / spanCount + 1;
        for (int i = 0; i < count; i++) {
            int endTime = list.size() < (i + 1) * spanCount ? list.size() : (i + 1) * spanCount;
            int startTime = i == 0 ? i : i * spanCount;
            List<String> childList = list.subList(startTime, endTime);
            map.put(i, childList);
        }
        return map;
    }

    private void addRadioButton(int i) {
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(ScreenUtils.dip2px(this, 5), ScreenUtils.dip2px(this, 5));
        lp.setMargins(0, 0, ScreenUtils.dip2px(this, 5), 0);
        radioGroup.addView(initRadioButton(i), lp);
    }

    private RadioButton initRadioButton(int i) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(getResources().getIdentifier("main_radioButton" + i, "id", getPackageName()));
        radioButton.setBackground(getResources().getDrawable(R.drawable.activity_main_checkbox_background));
        radioButton.setButtonDrawable(null);
        radioButton.setChecked(i == 0);
        return radioButton;
    }

    private ArrayList<MainViewPagerFragmentData> initFragmentDataById(List<String> names, int fragmentCount) {
        String[] fragmentItemsBackGround = getResources().getStringArray(R.array.main_fragment_background);
        List<String> listBackground = Arrays.asList(fragmentItemsBackGround);

        String[] fragmentItemsImage = getResources().getStringArray(R.array.main_fragment_image);
        List<String> listImage = Arrays.asList(fragmentItemsImage);
        ArrayList<MainViewPagerFragmentData> list1 = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            int backGroundId = getResources().getIdentifier(listBackground.get((fragmentCount - 1) * 4 + i), "drawable", getPackageName());
            int imageId = getResources().getIdentifier(listImage.get((fragmentCount - 1) * 4 + i), "drawable", getPackageName());
            if (backGroundId != 0 && imageId != 0) {
                list1.add(new MainViewPagerFragmentData(backGroundId, names.get(i), imageId));
            }
        }
        return list1;
    }

    private void setRadioButtonState(int position) {
        RadioButton radioButton = (RadioButton) findViewById(getResources().getIdentifier("main_radioButton" + position, "id", getPackageName()));
        radioButton.setChecked(true);
    }

    @Override
    protected void initListener() {
        mIvSetting.setOnClickListener(this);
        mIvFeedBack.setOnClickListener(this);
        layoutVideoCapture.setOnClickListener(this);
        layoutVideoEdit.setOnClickListener(this);
        checkPermissions();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_main_setting:
                AppManager.getInstance().jumpActivity(this, ParameterSettingActivity.class, null);
                return;
            case R.id.iv_main_feedback:
                AppManager.getInstance().jumpActivity(this, FeedBackActivity.class, null);
                return;
        }

        if (!hasAllPermission()) {
            clickedView = view;
            checkPermissions();
        } else {
            doClick(view);
        }
    }

    private void doClick(View view) {
        if (view == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_main_setting:
                AppManager.getInstance().jumpActivity(this, ParameterSettingActivity.class, null);
                break;

            case R.id.layout_video_capture:
                AppManager.getInstance().jumpActivity(this, CaptureActivity.class, null);
                break;

            case R.id.layout_video_edit:
                Bundle editBundle = new Bundle();
                editBundle.putInt("visitMethod", Constants.FROMMAINACTIVITYTOVISIT);
                editBundle.putInt("limitMediaCount", -1);
                AppManager.getInstance().jumpActivity(this, SelectMediaActivity.class, editBundle);
                break;
            default:
                String tag = (String) view.getTag();
                if (tag.equals(getResources().getString(R.string.douYinEffects))) {
                    AppManager.getInstance().jumpActivity(this, DouYinCaptureActivity.class, null);
                } else if (tag.equals(getResources().getString(R.string.particleEffects))) {
                    AppManager.getInstance().jumpActivity(this, ParticleCaptureActivity.class, null);
                } else if (tag.equals(getResources().getString(R.string.captureScene))) {
                    AppManager.getInstance().jumpActivity(this, CaptureSceneActivity.class, null);
                } else if (tag.equals(getResources().getString(R.string.picInPic))) {
                    Bundle pipBundle = new Bundle();
                    pipBundle.putInt("visitMethod", Constants.FROMPICINPICACTIVITYTOVISIT);
                    pipBundle.putInt("limitMediaCount", 2);
                    AppManager.getInstance().jumpActivity(this, SelectMediaActivity.class, pipBundle);
                } else if (tag.equals(getResources().getString(R.string.makingCover))) {
                    Bundle makeCoverBundle = new Bundle();
                    makeCoverBundle.putInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_IMAGE_FROM_MAKE_COVER);
                    AppManager.getInstance().jumpActivity(this, SingleClickActivity.class, makeCoverBundle);
                } else if (tag.equals(getResources().getString(R.string.flipSubtitles))) {
                    Bundle flipBundle = new Bundle();
                    flipBundle.putInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_VIDEO_FROM_FLIP_CAPTION);
                    flipBundle.putInt("limitMediaCount", -1);//-1表示无限可选择素材
                    AppManager.getInstance().jumpActivity(this, MultiVideoSelectActivity.class, flipBundle);
                } else if (tag.equals(getResources().getString(R.string.musicLyrics))) {
                    Bundle musicBundle = new Bundle();
                    musicBundle.putInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_VIDEO_FROM_MUSIC_LYRICS);
                    musicBundle.putInt("limitMediaCount", -1);//-1表示无限可选择素材
                    AppManager.getInstance().jumpActivity(this, MultiVideoSelectActivity.class, musicBundle);
                } else if (tag.equals(getResources().getString(R.string.boomRang))) {
                    AppManager.getInstance().jumpActivity(this, BoomRangActivity.class);
                } else if (tag.equals(getResources().getString(R.string.pushMirrorFilm))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        AppManager.getInstance().jumpActivity(this, SuperZoomActivity.class);
                    } else {
                        String[] tipsInfo = getResources().getStringArray(R.array.edit_function_tips);
                        Util.showDialog(MainActivity.this, tipsInfo[0], getString(R.string.versionBelowTip));
                    }
                } else {
                    String[] tipsInfo = getResources().getStringArray(R.array.edit_function_tips);
                    Util.showDialog(MainActivity.this, tipsInfo[0], tipsInfo[1], tipsInfo[2]);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasAllPermission()) {
            Util.clearRecordAudioData();
        }
        if (mStreamingContext != null) {//退出清理
            NvsStreamingContext.close();
            mStreamingContext = null;
            TimelineData.instance().clear();
            BackupData.instance().clear();
            // 获取当前进程的id
            int pid = android.os.Process.myPid();
            // 这个方法只能用于自杀操作
            android.os.Process.killProcess(pid);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 获取activity需要的权限列表
     *
     * @return 权限列表
     */
    @Override
    protected List<String> initPermissions() {
        return Util.getAllPermissionsList();
    }

    /**
     * 获取权限后
     */
    @Override
    protected void hasPermission() {
        Log.e("1234", "hasPermission: 所有权限都有了");
        doClick(clickedView);
    }

    /**
     * 没有允许权限
     */
    @Override
    protected void nonePermission() {
        Log.e("1234", "hasPermission: 没有允许权限");
    }

    /**
     * 用户选择了不再提示
     */
    @Override
    protected void noPromptPermission() {
        Log.e("1234", "hasPermission: 用户选择了不再提示");
        startAppSettings();
    }

    // 启动应用的设置
    public void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MobclickAgent.onKillProcess(this);
    }
}
