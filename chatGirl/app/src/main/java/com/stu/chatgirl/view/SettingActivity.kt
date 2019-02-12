package com.stu.chatgirl.view

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stu.chatgirl.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {
    override fun getTitleString(): CharSequence? {
        return "设置"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        tvBackTitle.text = titleString
        RecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        var list = listOf(Pair(R.mipmap.type, "聊天分类"), Pair(R.mipmap.clear, "清空缓存"), Pair(R.mipmap.version, "检查版本"), Pair(R.mipmap.about, "关于我的"))
        RecyclerView.adapter = SettingAdapter(R.layout.item_setting, list)
    }


    class SettingAdapter(layoutId: Int, listData: List<Pair<Int, String>>) : BaseQuickAdapter<Pair<Int, String>, BaseViewHolder>(layoutId, listData) {
        override fun convert(helper: BaseViewHolder?, item: Pair<Int, String>?) {
            item?.first?.let {
                helper?.setText(R.id.itemSettingTitle, item?.second)
                        ?.setImageResource(R.id.itemSettingImage, it)
            }
        }
    }
}
