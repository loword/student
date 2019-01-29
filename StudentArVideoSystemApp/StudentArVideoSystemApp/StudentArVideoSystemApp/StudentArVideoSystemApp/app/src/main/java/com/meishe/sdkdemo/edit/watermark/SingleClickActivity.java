package com.meishe.sdkdemo.edit.watermark;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BaseActivity;
import com.meishe.sdkdemo.douyin.DouyinTrimActivity;
import com.meishe.sdkdemo.edit.animatesticker.customsticker.CustomAnimateStickerActivity;
import com.meishe.sdkdemo.edit.data.BackupData;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.makecover.MakeCoverActivity;
import com.meishe.sdkdemo.selectmedia.bean.MediaData;
import com.meishe.sdkdemo.selectmedia.fragment.MediaFragment;
import com.meishe.sdkdemo.selectmedia.interfaces.OnTotalNumChangeForActivity;
import com.meishe.sdkdemo.selectmedia.view.CustomPopWindow;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.Constants;
import com.meishe.sdkdemo.utils.MediaConstant;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.dataInfo.ClipInfo;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;

import java.util.ArrayList;
import java.util.List;

import static com.meishe.sdkdemo.utils.Constants.POINT16V9;
import static com.meishe.sdkdemo.utils.Constants.POINT1V1;
import static com.meishe.sdkdemo.utils.Constants.POINT3V4;
import static com.meishe.sdkdemo.utils.Constants.POINT4V3;
import static com.meishe.sdkdemo.utils.Constants.POINT9V16;
import static com.meishe.sdkdemo.utils.MediaConstant.KEY_CLICK_TYPE;
import static com.meishe.sdkdemo.utils.MediaConstant.SINGLE_PICTURE_PATH;

public class SingleClickActivity extends BaseActivity implements OnTotalNumChangeForActivity, CustomPopWindow.OnViewClickListener {
    private final String TAG = "SingleClickActivity";
    private CustomTitleBar m_titleBar;
    private TextView sigleTvStartEdit;
    private List<MediaData> mediaDataList;
    private int fromWhat = Constants.SELECT_IMAGE_FROM_WATER_MARK;

    //画中画专用
    private int mPicInPicVideoIndex = -1;
    @Override
    protected int initRootView() {
        return R.layout.activity_single_click;
    }

    @Override
    protected void initViews() {
        m_titleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        sigleTvStartEdit = (TextView) findViewById(R.id.sigle_tv_startEdit);
        sigleTvStartEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromWhat == Constants.SELECT_IMAGE_FROM_WATER_MARK) {
                    Intent intent = new Intent();
                    intent.putExtra(SINGLE_PICTURE_PATH, mediaDataList.get(0).getPath());
                    setResult(RESULT_OK, intent);
                    AppManager.getInstance().finishActivity();
                } else if (fromWhat == Constants.SELECT_IMAGE_FROM_MAKE_COVER) {
                    new CustomPopWindow.PopupWindowBuilder(SingleClickActivity.this)
                            .setView(R.layout.pop_select_makesize)
                            .setViewClickListener(SingleClickActivity.this)
                            .create()
                            .showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
                } else if (fromWhat == Constants.SELECT_IMAGE_FROM_CUSTOM_STICKER) {
                    Bundle bundle = new Bundle();
                    bundle.putString("imageSrcFilePath", mediaDataList.get(0).getPath());
                    AppManager.getInstance().jumpActivity(AppManager.getInstance().currentActivity(), CustomAnimateStickerActivity.class, bundle);
                }else if(fromWhat == Constants.SELECT_VIDEO_FROM_DOUYINCAPTURE){
                    long duration = 1000 * mediaDataList.get(0).getDuration();
                    long minDuration = 3 * Constants.NS_TIME_BASE;
                    if(duration < minDuration){
                        String[] selectTips = getResources().getStringArray(R.array.select_video_min_duration_tips);
                        Util.showDialog(SingleClickActivity.this, selectTips[0], selectTips[1]);
                        return;
                    }
                    int makeRatio = NvAsset.AspectRatio_9v16;
                    TimelineData.instance().setVideoResolution(Util.getVideoEditResolution(makeRatio));
                    TimelineData.instance().setMakeRatio(makeRatio);
                    Bundle bundle = new Bundle();
                    bundle.putString("videoFilePath", mediaDataList.get(0).getPath());
                    AppManager.getInstance().jumpActivity(AppManager.getInstance().currentActivity(), DouyinTrimActivity.class, bundle);
                    AppManager.getInstance().finishActivity();
                }else if(fromWhat == Constants.FROMPICINPICACTIVITYTOVISIT){//画中画页面
                    if(mPicInPicVideoIndex >= 0){
                        //替换视频
                        BackupData.instance().getPicInPicVideoArray().remove(mPicInPicVideoIndex);
                        BackupData.instance().getPicInPicVideoArray().add(mPicInPicVideoIndex,mediaDataList.get(0).getPath());
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        AppManager.getInstance().finishActivity();
                    }
                }
            }
        });
    }

    @Override
    protected void initTitle() {
        m_titleBar.setTextCenter(R.string.selectMedia);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                fromWhat = bundle.getInt(Constants.SELECT_MEDIA_FROM, Constants.SELECT_IMAGE_FROM_WATER_MARK);
                mPicInPicVideoIndex = bundle.getInt("picInPicVideoIndex",-1);
                if(fromWhat == Constants.SELECT_VIDEO_FROM_DOUYINCAPTURE
                        || fromWhat == Constants.FROMPICINPICACTIVITYTOVISIT){
                    // 抖音拍摄页面相册入口
                    m_titleBar.setTextCenter(R.string.select_video);
                }else {//封面, 水印,自定义贴纸
                    m_titleBar.setTextCenter(R.string.single_select_picture);
                }
            }
        }
        initVideoFragment(R.id.single_contain);
    }

    @Override
    public void onViewClick(CustomPopWindow popWindow, View view) {
        switch (view.getId()) {
            case R.id.button16v9:
                selectCreateRatio(POINT16V9);
                break;
            case R.id.button1v1:
                selectCreateRatio(POINT1V1);
                break;
            case R.id.button9v16:
                selectCreateRatio(POINT9V16);
                break;
            case R.id.button3v4:
                selectCreateRatio(POINT3V4);
                break;
            case R.id.button4v3:
                selectCreateRatio(POINT4V3);
            default:
                break;
        }
    }

    private void selectCreateRatio(int makeRatio) {
        ArrayList<ClipInfo> pathList = getClipInfoList();
        TimelineData.instance().setVideoResolution(Util.getVideoEditResolution(makeRatio));
        TimelineData.instance().setClipInfoData(pathList);
        TimelineData.instance().setMakeRatio(makeRatio);
        AppManager.getInstance().jumpActivity(SingleClickActivity.this, MakeCoverActivity.class, null);
    }

    private ArrayList<ClipInfo> getClipInfoList() {
        ArrayList<ClipInfo> pathList = new ArrayList<>();
        if (mediaDataList != null) {
            for (MediaData mediaData : mediaDataList) {
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
        if(fromWhat == Constants.SELECT_VIDEO_FROM_DOUYINCAPTURE
                || fromWhat == Constants.FROMPICINPICACTIVITYTOVISIT){
            // 抖音拍摄页面相册入口或者画中画替换视频入口
            bundle.putInt(MediaConstant.MEDIA_TYPE, MediaConstant.VIDEO);
        }else {//封面, 水印,自定义贴纸
            bundle.putInt(MediaConstant.MEDIA_TYPE, MediaConstant.IMAGE);
        }
        bundle.putInt(KEY_CLICK_TYPE, MediaConstant.TYPE_ITEMCLICK_SINGLE);
        mediaFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(layoutId, mediaFragment)
                .commit();
        getSupportFragmentManager().beginTransaction().show(mediaFragment);
    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTotalNumChangeForActivity(List selectList, Object tag) {
        mediaDataList = selectList;
        sigleTvStartEdit.setVisibility(selectList.size() > 0 ? View.VISIBLE : View.GONE);
    }
}
