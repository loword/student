package com.stu.chatgirl.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kaopiz.kprogresshud.KProgressHUD
import com.stu.chatgirl.R
import kotlinx.android.synthetic.main.activity_setting.*


class SettingGrilActivity : BaseGrilActivity() {
    override fun getTitleString(): CharSequence? {
        return "设置"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.stu.chatgirl.R.layout.activity_setting)
        tvBackTitle.text = titleString
        RecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        var list = listOf(Pair(com.stu.chatgirl.R.mipmap.type, "聊天用法"), Pair(com.stu.chatgirl.R.mipmap.clear, "清空缓存"), Pair(R.mipmap.version, "检查版本"), Pair(R.mipmap.about, "关于我的"))
        var settingAdapter = SettingAdapter(R.layout.item_setting, list)
        RecyclerView.adapter = settingAdapter
        settingAdapter.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {
                    startActivity(Intent(this, TypeGrilActivity::class.java))
                }

                1 -> {
                    var show = KProgressHUD.create(this@SettingGrilActivity)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("请稍等")
                            .setDetailsLabel("正在清空缓存 ... ")
                            .setCancellable(true)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show()

                    val handler = Handler()
                    handler.postDelayed({
                        show.dismiss()
                        Toast.makeText(this@SettingGrilActivity, "已经清空", Toast.LENGTH_SHORT).show()
                    }, 2000)

                }

                2 -> {
                    var show = KProgressHUD.create(this@SettingGrilActivity)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("请稍等")
                            .setDetailsLabel("正在请求服务器中 ... ")
                            .setCancellable(true)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show()

                    val handler = Handler()
                    handler.postDelayed({
                        show.dismiss()
                        Toast.makeText(this@SettingGrilActivity, "已是最新版本", Toast.LENGTH_SHORT).show()
                    }, 2000)

                }

                3 -> {
                    startActivity(Intent(this, AboutGrilGrilActivity::class.java))
                }

                else -> {
                }
            }
        }
    }


    class SettingAdapter(layoutId: Int, listData: List<Pair<Int, String>>) : BaseQuickAdapter<Pair<Int, String>, BaseViewHolder>(layoutId, listData) {
        override fun convert(helper: BaseViewHolder?, item: Pair<Int, String>?) {
            item?.first?.let {
                helper?.setText(com.stu.chatgirl.R.id.itemSettingTitle, item?.second)
                        ?.setImageResource(com.stu.chatgirl.R.id.itemSettingImage, it)
            }
        }
    }
}
