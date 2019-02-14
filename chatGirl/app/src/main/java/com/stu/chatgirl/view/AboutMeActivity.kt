package com.stu.chatgirl.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import com.stu.chatgirl.R
import com.stu.chatgirl.utils.StatusBarUtils
import kotlinx.android.synthetic.main.activity_splash_main.*

class AboutMeActivity : BaseActivity() {
    override fun getTitleString(): CharSequence? {
        return "关于我们"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_main)
        one.visibility = View.VISIBLE
        two.visibility = View.VISIBLE
    }
}
