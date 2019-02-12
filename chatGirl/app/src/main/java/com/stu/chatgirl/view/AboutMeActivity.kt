package com.stu.chatgirl.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.stu.chatgirl.R
import com.stu.chatgirl.utils.StatusBarUtils

class AboutMeActivity : BaseActivity() {
    override fun getTitleString(): CharSequence? {
        return "关于我们"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
