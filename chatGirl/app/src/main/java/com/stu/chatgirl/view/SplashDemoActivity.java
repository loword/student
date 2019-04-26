package com.stu.chatgirl.view;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stu.chatgirl.R;
import com.stu.chatgirl.utils.SharedPreferencesUtils;


/**
 * @author peterliu
 */
public class SplashDemoActivity extends AppCompatActivity {

    private int time = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_main);
        final boolean isFirstInApp = false;
        time = isFirstInApp ? 1000 : 500;
        final Intent intent = new Intent(SplashDemoActivity.this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstInApp) {

                    new MaterialDialog.Builder(SplashDemoActivity.this)
                            .content(R.string.content)
                            .title(R.string.title)
                            .positiveText(R.string.boy)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {// boy
                                    SharedPreferencesUtils.setParam(SplashDemoActivity.this, "sex", true);
                                }
                            })
                            .cancelable(false)
                            .negativeText(R.string.girl)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) { // girl
                                    SharedPreferencesUtils.setParam(SplashDemoActivity.this, "sex", false);
                                }
                            })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();

                } else {
                    startActivity(intent);
                    finish();
                }
            }
        }, time);
    }
}
