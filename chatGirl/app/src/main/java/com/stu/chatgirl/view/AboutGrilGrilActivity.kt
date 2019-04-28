package com.stu.chatgirl.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.stu.chatgirl.R
import com.stu.chatgirl.view.edit.EditShowInfoActivity
import kotlinx.android.synthetic.main.activity_splash_main.*

class AboutGrilGrilActivity : BaseGrilActivity() {
    override fun getTitleString(): CharSequence? {
        return "关于我们"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_main)
        one.visibility = View.VISIBLE
        two.visibility = View.VISIBLE
        tvShowInfo.visibility = View.VISIBLE
        tvShowInfo.setOnClickListener {
            startActivity(Intent(this, EditShowInfoActivity::class.java))
        }
    }
}
