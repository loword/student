package com.meishe.paintbrush.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by ms on 2018/10/12.
 */

public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private List<String> mTabTitles;
    public BaseFragmentPagerAdapter(FragmentManager fm, List<Fragment> list, List<String> tabTitles) {
        super(fm);
        mFragmentList=list;
        mTabTitles=tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles.get(position);
    }
}