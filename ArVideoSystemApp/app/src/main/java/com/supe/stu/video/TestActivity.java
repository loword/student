package com.supe.stu.video;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.baidu.ar.bean.DuMixARConfig;

public class TestActivity extends Activity {

    private static final int REQUEST_CODE_CAMERA = 1001;
    private String[] mPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DuMixARConfig.setAppId("14843937");
        DuMixARConfig.setAPIKey("3s3gtdIquPA4AghSNCXUArux");
        DuMixARConfig.setSecretKey("EMh8cN9IrG9GZkkG4vIlnCGDzpWl1IyF");

        findViewById(R.id.tv_click_start_ar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceARApplication.canClick()) {
                    return;
                }
                boolean mHasPermission = true;
                for (String permission : mPermissions) {
                    if (ContextCompat.checkSelfPermission(TestActivity.this, permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        mHasPermission = false;
                        break;
                    }
                }
                if (!mHasPermission) {
                    ActivityCompat.requestPermissions(TestActivity.this, mPermissions, REQUEST_CODE_CAMERA);
                } else {
                    ARActivity.startActivity(TestActivity.this);
                    overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        if (requestCode == REQUEST_CODE_CAMERA) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted) {
                if (!FaceARApplication.canClick()) {
                    return;
                }
                Intent intent = new Intent(TestActivity.this, ARActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        }
    }
}
