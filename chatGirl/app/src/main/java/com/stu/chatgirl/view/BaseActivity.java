package com.stu.chatgirl.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.stu.chatgirl.R;
import com.stu.chatgirl.utils.StatusBarUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void exitCurrentActivity(View view) {
        finish();
    }

    @org.jetbrains.annotations.Nullable
    public abstract CharSequence getTitleString();
}
