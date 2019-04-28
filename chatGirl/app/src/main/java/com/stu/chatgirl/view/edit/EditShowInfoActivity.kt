package com.stu.chatgirl.view.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.stu.chatgirl.R
import com.stu.chatgirl.view.BaseGrilActivity
import kotlinx.android.synthetic.main.activity_setting.*

/**
 *  @author by peter liu
 *  time on 2019-04-27 16:18
 */
class EditShowInfoActivity : BaseGrilActivity() {
    var hobby = 4
    val sign = 1
    var birthday = 3
    var sex = 5

    override fun getTitleString(): CharSequence? {
        return "我的资料"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        tvBackTitle.text = titleString
        var view = LayoutInflater.from(this).inflate(R.layout.activity_edit_me, null)
        container.addView(view)
        var intent = Intent(this, EditViewActivity::class.java)
        view.findViewById<TextView>(R.id.tvSign).let {
            it.setOnClickListener {
                intent.putExtra("type", 1)
                startActivity(intent)
            }
        }
        view.findViewById<TextView>(R.id.sex).let {
            it.setOnClickListener {
                intent.putExtra("type", 5)
                startActivity(intent)
            }
        }
        view.findViewById<TextView>(R.id.birthday).let {
            it.setOnClickListener {
                intent.putExtra("type", 3)
                startActivity(intent)
            }
        }
        view.findViewById<TextView>(R.id.hobby).let {
            it.setOnClickListener {
                intent.putExtra("type", 4)
                startActivity(intent)
            }
        }
    }
}
