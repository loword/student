package com.supe.stu.video.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.ar.util.Res;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baidu on 2018/4/15.
 */

public class FaceFilterStyleRecyAdapter extends RecyclerView.Adapter<FaceFilterStyleRecyAdapter.FaceFilterHolder> {

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }



    public static class FaceFilterHolder extends RecyclerView.ViewHolder{

        private TextView mTitle;
        private ImageView mImage;
        private View  convertView;
        public FaceFilterHolder(View v) {
            super(v);
            convertView = v;
             mTitle = v.findViewById(Res.id("text_list_item"));
             mImage = v.findViewById(Res.id("img_list_item"));
        }

        public void changeView(boolean selected){
            convertView.setSelected(selected);
        }
    }


    private int mSelectIndex = -1;
    private List<FaceRes> mFaceFilters = new ArrayList<>();

    private String[] mFilterThumb = new String[]{"filter_memory", "filter_girl", "filter_red",
            "filter_city", "filter_light", "filter_lip", "filter_neon",
            "normal", "beyond", "fresh_5", "hide", "hts1", "hts2", "hts3", "hts5",
            "hts9", "lively", "beach_15", "pure", "romeo", "rose", "selffilter", "sincere",
            "snow", "teen", "uncommon", "vivid_4", "years", "lut100", "lut101", "lut102", "lut103",
            "lut104", "lut105", "lut106", "lut107", "lut108", "lut109", "lut110", "lut111"};

    private String[] mFilterName = new String[]{"回忆", "少女", "红润", "都市", "微光", "红唇", "霓虹",
            "normal", "beyond", "fresh_5", "hide", "hts1", "hts2", "hts3", "hts5", "hts9", "lively",
            "beach_15", "pure", "romeo", "rose", "selffilter", "sincere", "snow", "teen", "uncommon", "vivid_4",
            "years", "lut100", "lut101", "lut102", "lut103", "lut104", "lut105", "lut106", "lut107", "lut108",
            "lut109", "lut110", "lut111"};
    private String[] mFilterId = new String[]{"500034", "500035", "500036", "500037", "500038", "500039", "500040",
            "500001", "500002", "500003", "500004", "500005", "500006", "500007", "500008", "500009", "500010",
            "500011", "500012", "500013", "500014", "500015", "500016", "500017", "500018", "500019", "500020",
            "500021", "500022", "500023", "500024", "500025", "500026", "500027", "500028", "500029", "500030",
            "500031", "500032", "500033"};

    public FaceFilterStyleRecyAdapter(int fileCount) {
        for (int i = 0; i < mFilterThumb.length; i++) {
            FaceRes res = new FaceRes();
            res.index = i;
            res.name = mFilterName[i];
            res.thumbnail = Res.drawableIId(mFilterThumb[i]);
            res.resPath = mFilterId[i];
            mFaceFilters.add(res);
        }
    }


    public FaceRes onClickPosition(int index) {
        if (index == mSelectIndex) {
            mSelectIndex = -1;
            return null;
        }

        mSelectIndex = index;
        return mFaceFilters.get(mSelectIndex);

    }

    @Override
    public FaceFilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {

       View view = Res.inflate("bdar_face_filter_item");
        return new FaceFilterHolder(view);
    }


    @Override
    public void onBindViewHolder(FaceFilterHolder holder, final int position) {
        if (position == mSelectIndex) {
            holder.mImage.setBackground(Res.getDrawable("bdar_face_filter_selected"));
//            convertView.setSelected(true);
            holder.changeView(true);
        } else {
            holder.mImage.setBackground(null);
//            convertView.setSelected(false);
            holder.changeView(false);
        }

        holder.mTitle.setVisibility(View.VISIBLE);
        holder.mTitle.setText(mFaceFilters.get(position).name);
        holder.mImage.setImageResource(mFaceFilters.get(position).thumbnail);

        holder.mImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mFaceFilters.size();
    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup viewGroup) {
//        ViewHolder holder;
//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = Res.inflate("bdar_face_filter_item");
//            holder.mImage = (ImageView) convertView.findViewById(Res.id("img_list_item"));
//            holder.mTitle = (TextView) convertView.findViewById(Res.id("text_list_item"));
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        if (position == mSelectIndex) {
//            holder.mImage.setBackground(Res.getDrawable("bdar_face_filter_selected"));
//            convertView.setSelected(true);
//        } else {
//            holder.mImage.setBackground(null);
//            convertView.setSelected(false);
//        }
//
//        holder.mTitle.setVisibility(View.VISIBLE);
//        holder.mTitle.setText(getItem(position).name);
//        holder.mImage.setImageResource(getItem(position).thumbnail);
//        return convertView;
//    }
//
//    static class ViewHolder {
//        private TextView mTitle;
//        private ImageView mImage;
//    }

}
