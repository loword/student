package com.supe.stu.video.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.ar.util.Res;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baidu on 2018/4/15.
 */

public class FaceStickerAdapter extends BaseAdapter {
    private int mSelectIndex = -1;
    private List<FaceRes> faceStickers = new ArrayList<>();

    private String[] stickerDir = new String[] {
            "example_2d_v02_src",
            "xiaomaomi_v01_src",
            "peiqi_v02_src", "huangguan_fense_v02_src", "aisi_v02_src",
            "chaoliuyanjing_v02_src",
            "chixigua_v02_gai_src",
            "erkang_v02_src",
            "kejimao_v03_src",
            "fengkaobiguo_v02_src",
            "huajidun_v01_src",
            "huangguan_v03_src",
            "kakaxi_v01_src",
            "kenan_v02_src",
            "pingdiguo_v01_src",
            "setaoxin_v02_src",
            "toukui_v02_src",
            "xiaolu_v01_src",
            "xiaoxingxing_v01_src",
            "xiaoxiongmao_src",
            "xiongxiongbingjiling_v02_src",
            "yezhu_v02_src",
            "yinghua_v1_src",
            "zhishangpenwu_v01_src",
            "yinghua_v02_src"};

    private int[] stickerThumb = new int[] {
            Res.drawableIId("bdar_face_sticker_6"),
            Res.drawableIId("bdar_face_sticker_8"),
            Res.drawableIId("bdar_face_sticker_5"),
            Res.drawableIId("bdar_face_sticker_5"),
            Res.drawableIId("icon_video_aisi_d"),
            Res.drawableIId("icon_video_chaoyanjing_d"),
            Res.drawableIId("icon_video_chigua_d"),
            Res.drawableIId("erkang_icon"),
            Res.drawableIId("icon_video_gouhoukeji_d"),
            Res.drawableIId("icon_video_fanshu_d"),
            Res.drawableIId("icon_video_huajidun_d"),
            Res.drawableIId("huangguan"),
            Res.drawableIId("icon_video_huoyingkakaxi_d"),
            Res.drawableIId("icon_video_kenan_d"),
            Res.drawableIId("icon_video_pingdiguo_d"),
            Res.drawableIId("icon_video_sese_d"),
            Res.drawableIId("icon_video_toukui_d"),
            Res.drawableIId("xiaolu"),
            Res.drawableIId("xiaoxingxing"),
            Res.drawableIId("xiaoxiongmao"),
            Res.drawableIId("xiongxiongbinjilin"),
            Res.drawableIId("yezhu"),
            Res.drawableIId("yinghua"),
            Res.drawableIId("icon_video_zhishang_d"),
            Res.drawableIId("yinghua_w")};

    public FaceStickerAdapter(File[] stickers) {
        if (stickers == null || stickers.length <= 0) {
            return;
        }
        File parent = stickers[0].getParentFile();
        for (int i = 0; i < stickerThumb.length; i++) {
            FaceRes res = new FaceRes();
            res.index = i;

            res.thumbnail = stickerThumb[i];
            res.name = stickerDir[i];
            res.resPath = new File(parent, res.name).getAbsolutePath();
            faceStickers.add(res);
        }

    }

    public FaceRes onClickPosition(int index) {
        if (index == mSelectIndex) {
            mSelectIndex = -1;
            return null;
        }

        mSelectIndex = index;
        return faceStickers.get(mSelectIndex);

    }

    @Override
    public int getCount() {
        return faceStickers.size();
    }

    @Override
    public FaceRes getItem(int i) {
        return faceStickers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = Res.inflate("bdar_face_sticker_item");
            holder.mImage = (ImageView) convertView.findViewById(Res.id("img_list_item"));
            holder.mTitle = (TextView) convertView.findViewById(Res.id("text_list_item"));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == mSelectIndex) {
            holder.mImage.setBackground(Res.getDrawable("bdar_face_sticker_selected"));
            convertView.setSelected(true);
        } else {
            holder.mImage.setBackground(null);
            convertView.setSelected(false);
        }
        holder.mTitle.setText(getItem(position).name);
        holder.mImage.setImageResource(getItem(position).thumbnail);
        return convertView;
    }

    static class ViewHolder {
        private TextView mTitle;
        private ImageView mImage;
    }

}
