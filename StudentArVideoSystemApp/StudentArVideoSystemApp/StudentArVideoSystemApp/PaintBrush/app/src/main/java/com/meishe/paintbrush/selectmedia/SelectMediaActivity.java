package com.meishe.paintbrush.selectmedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.meishe.paintbrush.R;
import com.meishe.paintbrush.base.BaseActivity;
import com.meishe.paintbrush.base.BaseFragmentPagerAdapter;
import com.meishe.paintbrush.selectmedia.bean.MediaData;
import com.meishe.paintbrush.selectmedia.fragment.MediaFragment;
import com.meishe.paintbrush.selectmedia.interfaces.OnTotalNumChangeForActivity;
import com.meishe.paintbrush.utils.Constants;
import com.meishe.paintbrush.utils.CustomTitleBar;
import com.meishe.paintbrush.utils.Logger;
import com.meishe.paintbrush.utils.MediaConstant;

import java.util.ArrayList;
import java.util.List;

public class SelectMediaActivity extends BaseActivity implements OnTotalNumChangeForActivity {
    private String TAG = getClass().getName();
    private CustomTitleBar mTitleBar;
    private TabLayout tlSelectMedia;
    private ViewPager vpSelectMedia;
    private List<Fragment> fragmentLists = new ArrayList<>();
    private List<String> fragmentTabTitles = new ArrayList<>();
    private BaseFragmentPagerAdapter fragmentPagerAdapter;
    private int visitMethod = Constants.FROMMAINACTIVITYTOVISIT;
    private List<MediaData> mMediaDataList = new ArrayList<>();
    private static int total = 0;
    private TextView meidaTVOfStart;

    private int mLimiteMediaCount = -1;
    public static int getTotal() {
        return total;
    }

    public static void setTotal(int total) {
        SelectMediaActivity.total = total;
    }

    @Override
    protected int initRootView() {
        return R.layout.activity_select_media;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        tlSelectMedia = (TabLayout) findViewById(R.id.tl_select_media);
        vpSelectMedia = (ViewPager) findViewById(R.id.vp_select_media);
        meidaTVOfStart = (TextView) findViewById(R.id.media_tv_startEdit);
        meidaTVOfStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visitMethod == Constants.FROMMAINACTIVITYTOVISIT) {

                } else if (visitMethod == Constants.FROMCLIPEDITACTIVITYTOVISIT) {

                }else if(visitMethod == Constants.FROMPICINPICACTIVITYTOVISIT){

                }
            }
        });

    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.selectMedia);
    }

    public void setTitleText(int count) {
        if (count > 0) {
            String txt = getResources().getString(R.string.setSelectMedia);
            @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String txtWidthCount = String.format(txt, count);
            mTitleBar.setTextCenter(txtWidthCount);
        } else {
            mTitleBar.setTextCenter(R.string.selectMedia);
        }
    }

    @Override
    protected void initData() {
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                visitMethod = bundle.getInt("visitMethod", Constants.FROMMAINACTIVITYTOVISIT);
                mLimiteMediaCount = bundle.getInt("limitMediaCount",-1);
            }
        }
        String[] tabList = getResources().getStringArray(R.array.select_media);
        checkDataCountAndTypeCount(tabList, MediaConstant.MEDIATYPECOUNT);
        for (int i = 0; i < tabList.length; i++) {
            MediaFragment mediaFragment = new MediaFragment(this, this, MediaConstant.TYPE_ITEMCLICK_MULTIPLE);
            Bundle bundle = new Bundle();
            bundle.putInt(MediaConstant.MEDIA_TYPE, MediaConstant.MEDIATYPECOUNT[i]);
            bundle.putInt("limitMediaCount", mLimiteMediaCount);
            mediaFragment.setArguments(bundle);
            fragmentLists.add(mediaFragment);
            fragmentTabTitles.add(tabList[i]);
        }

        //禁止预加载
        vpSelectMedia.setOffscreenPageLimit(3);
        //测试提交
        fragmentPagerAdapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragmentLists, fragmentTabTitles);
        vpSelectMedia.setAdapter(fragmentPagerAdapter);
        vpSelectMedia.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                notifyFragmentDataSetChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tlSelectMedia.setupWithViewPager(vpSelectMedia);
    }

    /**
     * 校验一次数据，使得item标注的数据统一
     *
     * @param position 碎片对应位置0.1.2
     */
    private void notifyFragmentDataSetChanged(int position) {
        MediaFragment fragment = (MediaFragment) fragmentLists.get(position);
        List<MediaData> currentFragmentList = checkoutSelectList(fragment);
        fragment.getAdapter().setSelectList(currentFragmentList);
        setTitleText(fragment.getAdapter().getSelectList().size());
        Logger.e(TAG, "onPageSelected: " + fragment.getAdapter().getSelectList().size());
    }

    private List<MediaData> checkoutSelectList(MediaFragment fragment) {
        List<MediaData> currentFragmentList = fragment.getAdapter().getSelectList();
        List<MediaData> totalSelectList = getmMediaDataList();
        for (MediaData mediaData : currentFragmentList) {
            for (MediaData data : totalSelectList) {
                if (data.getPath().equals(mediaData.getPath()) && data.isState() == mediaData.isState()) {
                    mediaData.setPosition(data.getPosition());
                }
            }
        }
        return currentFragmentList;
    }

    private void checkDataCountAndTypeCount(String[] tabList, int[] mediaTypeCount) {
        if (tabList.length != mediaTypeCount.length) {
            return;
        }
    }

    @Override
    protected void initListener() {
        
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //判断如果同意的情况下就去 吧权限请求设置给当前fragment的
        for (int i = 0; i < fragmentLists.size(); i++) {
            fragmentLists.get(i).onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onTotalNumChangeForActivity(List selectList, Object tag) {
        if(visitMethod == Constants.FROMPICINPICACTIVITYTOVISIT){//画中画页面
            meidaTVOfStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
            List<MediaData> mediaDataList = getmMediaDataList();
            int count = mediaDataList.size();
            meidaTVOfStart.setTextColor(count == mLimiteMediaCount ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ffa3a3a3"));
            meidaTVOfStart.setEnabled(count == mLimiteMediaCount ? true : false);
        }else {
            meidaTVOfStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        }

        int index = (int) tag;
        Logger.e("2222", "onTotalNumChangeForActivity对应的碎片：  " + index);
        for (int i = 0; i < fragmentLists.size(); i++) {
            if (i != index) {
                Logger.e("2222", "要刷新的碎片：  " + i);
                MediaFragment fragment = (MediaFragment) fragmentLists.get(i);
                fragment.refreshSelect(selectList, index);
            }
        }
        Logger.e(TAG, "onTotalNumChangeForActivity  " + selectList.size());
    }


    public List<MediaData> getmMediaDataList() {
        if (mMediaDataList == null) {
            return new ArrayList<>();
        }
        MediaFragment fragment = (MediaFragment) fragmentLists.get(0);
        return fragment.getAdapter().getSelectList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        setTotal(0);
        super.onDestroy();
        Logger.e(TAG, "onDestroy");
    }
}
