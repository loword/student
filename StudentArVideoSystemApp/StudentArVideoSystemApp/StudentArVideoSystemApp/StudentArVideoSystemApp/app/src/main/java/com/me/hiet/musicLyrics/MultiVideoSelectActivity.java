package com.me.hiet.musicLyrics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.me.hiet.utils.dataInfo.TimelineData;
import com.meishe.sdkdemo.R;
import com.me.hiet.hite.BaseActivity;
import com.me.hiet.edit.view.CustomTitleBar;
import com.me.hiet.flipcaption.FlipCaptionActivity;
import com.me.hiet.selectmedia.bean.MediaData;
import com.me.hiet.selectmedia.fragment.MediaFragment;
import com.me.hiet.selectmedia.interfaces.OnTotalNumChangeForActivity;
import com.me.hiet.utils.AppManager;
import com.me.hiet.utils.Constants;
import com.me.hiet.utils.Logger;
import com.me.hiet.utils.MediaConstant;
import com.me.hiet.utils.Util;
import com.me.hiet.utils.dataInfo.ClipInfo;

import java.util.ArrayList;
import java.util.List;

import static com.me.hiet.utils.Constants.POINT9V16;
import static com.me.hiet.utils.MediaConstant.KEY_CLICK_TYPE;
import static com.me.hiet.utils.MediaConstant.LIMIT_COUNT;

public class MultiVideoSelectActivity extends BaseActivity implements OnTotalNumChangeForActivity{
    private final String TAG = "MultiVideoSelectActivity";
    private CustomTitleBar mTitleBar;
    private TextView sigleTvStartEdit;
    private List<MediaData> mMediaDataList;
    private int fromWhat = Constants.SELECT_VIDEO_FROM_MUSIC_LYRICS;
    private int mLimiteMediaCount = -1;
    @Override
    protected int initRootView() {
        return R.layout.activity_single_click;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        sigleTvStartEdit = (TextView) findViewById(R.id.sigle_tv_startEdit);

        sigleTvStartEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCreateRatio(POINT9V16);
                AppManager.getInstance().finishActivity();
            }
        });
    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.selectMedia);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                fromWhat = bundle.getInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_VIDEO_FROM_MUSIC_LYRICS);
                mLimiteMediaCount = bundle.getInt("limitMediaCount",-1);
                mTitleBar.setTextCenter(R.string.select_video);
            }
        }

        initVideoFragment(R.id.single_contain);
    }

    private void selectCreateRatio(int makeRatio) {
        ArrayList<ClipInfo> pathList = getClipInfoList();
        TimelineData.instance().setVideoResolution(Util.getVideoEditResolution(makeRatio));
        TimelineData.instance().setClipInfoData(pathList);
        TimelineData.instance().setMakeRatio(makeRatio);
        if (fromWhat == Constants.SELECT_VIDEO_FROM_MUSIC_LYRICS) {
            AppManager.getInstance().jumpActivity(MultiVideoSelectActivity.this, MusicLyricsActivity.class, null);
        }else if(fromWhat == Constants.SELECT_VIDEO_FROM_FLIP_CAPTION) {
            AppManager.getInstance().jumpActivity(MultiVideoSelectActivity.this, FlipCaptionActivity.class, null);
        }
    }

    private ArrayList<ClipInfo> getClipInfoList() {
        ArrayList<ClipInfo> pathList = new ArrayList<>();
        if (mMediaDataList != null) {
            for (MediaData mediaData : mMediaDataList) {
                ClipInfo clipInfo = new ClipInfo();
                clipInfo.setImgDispalyMode(Constants.EDIT_MODE_PHOTO_TOTAL_DISPLAY);
                clipInfo.setOpenPhotoMove(false);
                clipInfo.setFilePath(mediaData.getPath());
                pathList.add(clipInfo);
            }
        }
        return pathList;
    }

    private void initVideoFragment(int layoutId) {
        MediaFragment mediaFragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(MediaConstant.MEDIA_TYPE, MediaConstant.VIDEO);
        bundle.putInt(LIMIT_COUNT, mLimiteMediaCount);
        bundle.putInt(KEY_CLICK_TYPE, MediaConstant.TYPE_ITEMCLICK_MULTIPLE);
        mediaFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(layoutId, mediaFragment)
                .commit();
        getSupportFragmentManager().beginTransaction().show(mediaFragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");
    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTotalNumChangeForActivity(List selectList, Object tag) {
        mMediaDataList = selectList;
        sigleTvStartEdit.setVisibility(selectList.size() > 0 ? View.VISIBLE : View.GONE);
    }

}
