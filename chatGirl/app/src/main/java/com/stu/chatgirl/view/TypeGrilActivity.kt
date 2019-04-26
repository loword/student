package com.stu.chatgirl.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stu.chatgirl.R
import kotlinx.android.synthetic.main.activity_setting.*

class TypeGrilActivity : BaseGrilActivity() {
    override fun getTitleString(): CharSequence? {
        return "聊天介绍"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        tvBackTitle.text = titleString
        RecyclerView.layoutManager = GridLayoutManager(this, 2)
        var list = listOf(Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon010.png", "生活百科"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon014.png", "图片搜索"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon005.png", "数字计算"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon011.png", "问答百科"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon022.png", "中英互译"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon015.png", "笑话大全"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon009.png", "故事大全"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon007.png", "星座运势"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon026.png", "急转弯"),
                Pair("http://file.tuling123.com/upload/image/icons/nlsc_icon032.png", "歇后语"))


        var keyContentList = listOf(
                Pair("提供对生活小常识、生活小百科的数据查询\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“玉米”", "玉米"),

                Pair("搜索互联网上的图片，海量图片精准匹配\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“刘亦菲图片”", "刘亦菲图片"),
                Pair("可进行乘方、开方、指数、对数、、统计等方面的运算\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“1+1”", "1+1"),
                Pair("百度百科现有的知识信息的查询，百度百科已经收录了超过1300多万的词条，参与词条编辑的网友超过580万人，" +
                        "几乎涵盖了所有已知的知识领域。\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“周杰伦是谁”", "周杰伦是谁"),
                Pair("提供中文和英语的互译服务。可以通过使用中英互译功能为用户提供实时优质的翻译服务，提升产品体验。您只需要通过开启此功能，传入待翻译的内容，并指定要翻译的源语言和目标语言种类，就可以得到相应的翻译结果。\n" +
                        "\n" +
                        " \n" +
                        "\n" +
                        "使用示例：" +
                        "输入“苹果的单词是什么”", "苹果的单词是什么"),
                Pair("笑话是篇幅短小，故事情节简单而巧妙，往往出人意料，给人突然之间笑神来了的奇妙感觉，取得笑的艺术效果。其趣味有高下之分。此功能可随机返回互联网的幽默、内涵的笑话与段子。\n" +
                        "\n" +
                        " \n" +
                        "\n" +
                        "使用示例：" +
                        "输入“讲个笑话”", "讲个笑话"),
                Pair("可随机提供成语故事、寓言故事、童话故事等多种类的故事内容，种类丰富，资源海量\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“讲个故事”", "讲个故事"),


                Pair("提供十二星座查询，每个星座的今天、明天、本周、本月、本年星座运势查询，时刻掌握星运动向\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“双子座的运势”", "双子座的运势"),

                Pair("随机返回脑筋急转弯数据，脑筋急转弯最早起源于古代印度。就是指当思维遇到特殊的阻碍时，\" +\n" +
                        "                        \"要很快的离开习惯的思路，从别的方面来思考问题。现在泛指一些不能用通常的思路来回答的智力问答题。\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“说个脑筋急转弯”", "说个脑筋急转弯"),


                Pair("可随机提供成语故事、寓言故事、童话故事等多种类的故事内容，种类丰富，资源海量\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“讲个故事”", "讲个故事"),


                Pair("歇后语是中国劳动人民自古以来在生活实践中创造的一种特殊语言形式，是一种短小、风趣、形象的语句。它由前后两部分组成：前一部分起“引子”作用，像谜面，后一部分起“后衬”的作用，像谜底，十分自然贴切。此功能数据种类齐全，包含节气、季节、动物、昆虫、人物、谐音、等方面，应有尽有。\n" +
                        "\n" +
                        "使用示例：" +
                        "输入“说个歇后语”", "说个歇后语"))


        var chatTypeAdapter = ChatTypeAdapter(R.layout.item_setting, list)
        RecyclerView.adapter = chatTypeAdapter
        chatTypeAdapter.setOnItemClickListener { adapter, view, position ->
            var intent = Intent(this, MainActivity::class.java)
            //生活百科

            MaterialDialog.Builder(this@TypeGrilActivity)
                    .content(keyContentList[position].first)
                    .title(list[position].second)
                    .positiveText(R.string.input)
                    .onPositive { dialog, which ->
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        intent.putExtra("chatKey", keyContentList[position].second)
                        startActivity(intent)
                    }
                    .cancelable(false)
                    .negativeText(R.string.no).show()
        }
    }


    class ChatTypeAdapter(layoutId: Int, listData: List<Pair<String, String>>) : BaseQuickAdapter<Pair<String, String>, BaseViewHolder>(layoutId, listData) {
        override fun convert(helper: BaseViewHolder?, item: Pair<String, String>?) {
            item?.first?.let {
                helper?.setText(R.id.itemSettingTitle, item?.second)
                Glide.with(mContext).load(it).override(68, 68).into(helper?.getView(R.id.itemSettingImage))
            }
        }
    }
}
