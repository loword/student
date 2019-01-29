package com.meishe.paintbrush.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.meishe.paintbrush.utils.AppManager;

/**
 * Created by ms on 2018/10/12.
 */

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener  {


    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        //把当前初始化的activity加入栈中
        AppManager.getInstance().addActivity(this);
        //设置视图
        setContentView(initRootView());
        initViews();
        initTitle();
        initData();
        initListener();
    }


    /************************** 公共功能封装 《前》 ****************************************/
    //初始化视图
    protected abstract int initRootView();
    //初始化视图组件
    protected abstract void initViews();
    //初始化头布局
    protected abstract void initTitle();
    //数据处理
    protected abstract void initData();
    //视图监听事件处理
    protected abstract void initListener();


    /************************** 公共功能封装 《后》 ****************************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从堆栈中移除
        AppManager.getInstance().finishActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}