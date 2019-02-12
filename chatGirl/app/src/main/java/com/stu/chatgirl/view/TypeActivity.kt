package com.stu.chatgirl.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.stu.chatgirl.R

class TypeActivity : BaseActivity() {
    override fun getTitleString(): CharSequence? {
        return "聊天分类"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type)
    }
}
