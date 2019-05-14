package com.netease.nim.student.question;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.student.work.question.fragment.QuestionFragment;
import com.student.work.question.fragment.SearchFragment;

/**
 * @author peterliu
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv1, tv2, tv3, tv4;
    private Fragment fragment;
    private final int QUE = 1, COLLECT = 2, WRONG = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        initView();
        tv1 = findViewById(R.id.tv_search);
        tv1.setOnClickListener(this);
        tv2 = findViewById(R.id.tv_quest);
        tv2.setOnClickListener(this);
        tv3 = findViewById(R.id.tv_collect);
        tv3.setOnClickListener(this);
        tv4 = findViewById(R.id.tv_wrong);
        tv4.setOnClickListener(this);
        setDefaultFragment();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    //设置默认fragment
    private void setDefaultFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SearchFragment searchFragment = new SearchFragment();
        transaction.replace(R.id.content, searchFragment);
        transaction.commit();
        tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv2.setTextColor(getResources().getColor(R.color.gray));
        tv3.setTextColor(getResources().getColor(R.color.gray));
        tv4.setTextColor(getResources().getColor(R.color.gray));
        setTitle("查询");
    }

    //点击下栏切换界面
    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        fragment = getFragmentManager().findFragmentById(R.id.content);
        FragmentTransaction transaction = fm.beginTransaction();
        switch (v.getId()) {
            case R.id.tv_search://查询
                SearchFragment searchFragment = new SearchFragment();
                transaction.hide(fragment).replace(R.id.content, searchFragment);
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv2.setTextColor(getResources().getColor(R.color.gray));
                tv3.setTextColor(getResources().getColor(R.color.gray));
                tv4.setTextColor(getResources().getColor(R.color.gray));
                setTitle("查询");
                break;
            case R.id.tv_quest://题库
                QuestionFragment questionFragment = new QuestionFragment(QUE);
                transaction.hide(fragment).replace(R.id.content, questionFragment);
                tv1.setTextColor(getResources().getColor(R.color.gray));
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv3.setTextColor(getResources().getColor(R.color.gray));
                tv4.setTextColor(getResources().getColor(R.color.gray));
                setTitle("题库");
                break;
            case R.id.tv_collect://收藏
                QuestionFragment questionFragment1 = new QuestionFragment(COLLECT);
                transaction.hide(fragment).replace(R.id.content, questionFragment1);
                tv1.setTextColor(getResources().getColor(R.color.gray));
                tv2.setTextColor(getResources().getColor(R.color.gray));
                tv3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv4.setTextColor(getResources().getColor(R.color.gray));
                setTitle("收藏");
                break;
            case R.id.tv_wrong://错题
                QuestionFragment questionFragment2 = new QuestionFragment(WRONG);
                transaction.hide(fragment).replace(R.id.content, questionFragment2);
                tv1.setTextColor(getResources().getColor(R.color.gray));
                tv2.setTextColor(getResources().getColor(R.color.gray));
                tv3.setTextColor(getResources().getColor(R.color.gray));
                tv4.setTextColor(getResources().getColor(R.color.colorPrimary));
                setTitle("错题本");
                break;
        }
        transaction.commit();
    }
}
