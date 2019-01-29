package com.meishe.sdkdemo.capture;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.meishe.sdkdemo.R;


public class ShapeAdapter extends RecyclerView.Adapter<ShapeAdapter.ViewHolder> {

    private ArrayList<CaptureActivity.ShapeDataItem> mDataList;
    private int mSelectedPos = Integer.MAX_VALUE;
    private Context mContext;
    private OnItemClickListener mClickListener;
    private boolean mIsEnable = true;

    public ShapeAdapter(Context context, ArrayList dataList) {
        mContext = context;
        mDataList = dataList;
    }

    public void setEnable(boolean enable) {
        mIsEnable = enable;
        notifyDataSetChanged();
    }

    public void setSelectPos(int pos) {
        mSelectedPos = pos;
        notifyDataSetChanged();
    }

    public int getSelectPos() {
        return mSelectedPos;
    }

    public CaptureActivity.ShapeDataItem getSelectItem() {
        if (mDataList != null) {
            return mDataList.get(mSelectedPos);
        }
        return null;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void updateDataList(ArrayList dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beauty_shape_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CaptureActivity.ShapeDataItem item = mDataList.get(position);
        holder.shape_icon.setImageResource(item.resId);
        holder.shape_name.setText(item.name);

        if (mIsEnable) {
            holder.shape_name.setTextColor(Color.WHITE);
        } else {
            holder.shape_name.setTextColor(mContext.getResources().getColor(R.color.ms_disable_color));
        }

        if (mIsEnable && mSelectedPos == position) {
            holder.shape_name.setTextColor(Color.parseColor("#CC4A90E2"));
            holder.shape_icon_layout.setAlpha(1.0f);
            holder.shape_name.setAlpha(1.0f);
            GradientDrawable background = (GradientDrawable) holder.shape_icon_layout.getBackground();
            background.setColor(mContext.getResources().getColor(R.color.selcet_bg_blue));
        } else {
            if (mIsEnable && mSelectedPos != position) {
                GradientDrawable background = (GradientDrawable) holder.shape_icon_layout.getBackground();
                background.setColor(mContext.getResources().getColor(R.color.ms_disable_color));
                holder.shape_name.setTextColor(Color.WHITE);
                holder.shape_icon_layout.setAlpha(1.0f);
                holder.shape_name.setAlpha(1.0f);
            } else if (!mIsEnable) {
                GradientDrawable background = (GradientDrawable) holder.shape_icon_layout.getBackground();
                background.setColor(mContext.getResources().getColor(R.color.ms_disable_color));
                holder.shape_name.setTextColor(Color.WHITE);
                holder.shape_icon_layout.setAlpha(0.5f);
                holder.shape_name.setAlpha(0.5f);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsEnable) {
                    return;
                }
                if (mClickListener != null) {
                    notifyItemChanged(mSelectedPos);
                    mSelectedPos = position;
                    notifyItemChanged(mSelectedPos);
                    mClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout shape_icon_layout;
        private ImageView shape_icon;
        private TextView shape_name;

        public ViewHolder(View view) {
            super(view);
            shape_icon_layout = (RelativeLayout) view.findViewById(R.id.shape_icon_layout);
            shape_icon = (ImageView) view.findViewById(R.id.shape_icon);
            shape_name = (TextView) view.findViewById(R.id.shape_txt);
        }
    }

}
