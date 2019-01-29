package com.meishe.paintbrush.selectmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meishe.paintbrush.PaintBrushActivity;
import com.meishe.paintbrush.R;
import com.meishe.paintbrush.base.BaseActivity;
import com.meishe.paintbrush.selectmedia.bean.MediaData;
import com.meishe.paintbrush.selectmedia.fragment.MediaFragment;
import com.meishe.paintbrush.selectmedia.interfaces.OnTotalNumChangeForActivity;
import com.meishe.paintbrush.utils.ClipInfo;
import com.meishe.paintbrush.utils.CustomTitleBar;
import com.meishe.paintbrush.utils.MediaConstant;

import java.util.ArrayList;
import java.util.List;

public class SelectVideoActivity extends BaseActivity implements OnTotalNumChangeForActivity {
    private final String TAG = "SelectVideoActivity";
    private CustomTitleBar m_titleBar;
    private TextView sigleTvStartEdit;
    private List<MediaData> mediaDataList;

    @Override
    protected int initRootView() {
        return R.layout.activity_select_video;
    }

    @Override
    protected void initViews() {
        m_titleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        sigleTvStartEdit = (TextView) findViewById(R.id.sigle_tv_nextStep);
        sigleTvStartEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaDataList != null && mediaDataList.size() > 0) {
                    //跳转画笔制作
                    MediaData mediaData = mediaDataList.get(0);
                    if (mediaData.getDuration() < 3000) {
                        Toast.makeText(SelectVideoActivity.this, "请选择3秒以上的视频", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(SelectVideoActivity.this,PaintBrushActivity.class);
                    intent.putExtra("path", mediaData.getPath());
                    SelectVideoActivity.this.startActivity(intent);
                }else{
                    return;
                }
            }
        });
    }

    private void selectCreateRatio(int makeRatio) {
        //ArrayList<ClipInfo> pathList = getClipInfoList();
       // AppManager.getInstance().jumpActivity(SelectMediaActivity.this, VideoEditActivity.class, null);
    }

    // 片段信息-媒体文件路径
    private ArrayList<ClipInfo> getClipInfoList() {
        ArrayList<ClipInfo> pathList = new ArrayList<>();
        for (MediaData mediaData : mediaDataList) {
            ClipInfo clipInfo = new ClipInfo();
            clipInfo.setFilePath(mediaData.getPath());
            pathList.add(clipInfo);
        }
        return pathList;
    }

    @Override
    protected void initTitle() {
        m_titleBar.setTextCenter(R.string.select_video);
    }

    @Override
    protected void initData() {
        initVideoFragment(R.id.single_contain);
    }

    private void initVideoFragment(int layoutId) {
        MediaFragment mediaFragment = new MediaFragment(this, this, MediaConstant.TYPE_ITEMCLICK_SINGLE);
        Bundle bundle = new Bundle();
        bundle.putInt(MediaConstant.MEDIA_TYPE, MediaConstant.VIDEO);
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
        m_titleBar.setTextCenter(selectList.size() > 0 ? R.string.single_select_one : R.string.select_video);
    }

    @Override
    protected void onStop() {
        SelectMediaActivity.setTotal(0);
        super.onStop();
    }
}
