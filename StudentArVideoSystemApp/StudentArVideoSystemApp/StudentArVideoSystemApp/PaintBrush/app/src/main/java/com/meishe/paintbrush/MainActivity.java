package com.meishe.paintbrush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import com.meicam.sdk.NvsStreamingContext;
import com.meishe.paintbrush.base.BaseActivity;
import com.meishe.paintbrush.selectmedia.SelectVideoActivity;
import com.meishe.paintbrush.utils.AppManager;


public class MainActivity extends BaseActivity {
    public static final int REQUEST_CAMERA_PERMISSION_CODE = 200;
    public static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 201;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 202;
    public static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE = 203;
    private ImageButton mAddVideo;
    private boolean m_waitFlag;

    @Override
    protected int initRootView() {
        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return R.layout.activity_main;
        }
        //初始化
        NvsStreamingContext.init(this, null, NvsStreamingContext.STREAMING_CONTEXT_FLAG_SUPPORT_4K_EDIT);
        return R.layout.activity_main;
    }
    private void checkAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        } else {
                            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE);
                        }
                    } else {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                    }
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            }
        } else {

        }
    }
    @Override
    protected void initTitle() {

    }

    @Override
    protected void initViews() {
        mAddVideo = (ImageButton) findViewById(R.id.add_video);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        mAddVideo.setOnClickListener(this);
        checkAllPermission();
    }

    @Override
    public void onClick(View view) {
        if(m_waitFlag)
            return;

        switch (view.getId()) {
            case R.id.add_video://编辑
                m_waitFlag = true;
                AppManager.getInstance().jumpActivity(MainActivity.this, SelectVideoActivity.class, null);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_waitFlag = false;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            return;

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    } else
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION_CODE);
                }
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION_CODE:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE:
                break;
            case REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_CODE:

                break;
        }
    }
}
