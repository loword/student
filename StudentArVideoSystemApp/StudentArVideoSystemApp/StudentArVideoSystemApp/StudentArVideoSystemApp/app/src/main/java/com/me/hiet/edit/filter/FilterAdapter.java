package com.me.hiet.edit.filter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.me.hiet.edit.data.FilterItem;
import com.meishe.sdkdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyj on 2017/12/19 0019.
 */

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
    private Context mContext;
    private Boolean mIsArface = false;
    private OnItemClickListener mClickListener;
    private List<FilterItem> mFilterDataList = new ArrayList<>();
    RequestOptions mOptions = new RequestOptions();
    private int mSelectPos = 0;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public FilterAdapter(Context context) {
        mContext = context;
        mOptions.centerCrop();
        mOptions.skipMemoryCache(false);
        mOptions.placeholder(R.mipmap.default_filter);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setFilterDataList(List<FilterItem> filterDataList) {
        this.mFilterDataList = filterDataList;
    }

    public void setSelectPos(int pos) {
        this.mSelectPos = pos;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private View item_assetShadow;
        private ImageView item_assetImage;
        private TextView item_assetName;

        public ViewHolder(View view) {
            super(view);
            item_assetShadow = view.findViewById(R.id.assetShadow);
            item_assetName = (TextView) view.findViewById(R.id.nameAsset);
            item_assetImage = (ImageView) view.findViewById(R.id.imageAsset);
        }
    }

    public void isArface(Boolean isArface) {
        mIsArface = isArface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fx, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FilterItem itemData = mFilterDataList.get(position);
        if (itemData == null)
            return;
        String name = itemData.getFilterName();
        if (name != null && !mIsArface) {
            holder.item_assetName.setText(name);
        }
        if (mIsArface) {
            holder.item_assetName.setText("");
        }

        int filterMode = itemData.getFilterMode();
        if (filterMode == FilterItem.FILTERMODE_BUILTIN) {
            int imageId = itemData.getImageId();
            if (imageId != 0)
                holder.item_assetImage.setImageResource(imageId);
        } else {
            String imageUrl = itemData.getImageUrl();
            if (imageUrl != null) {
                //加载图片
                Glide.with(mContext)
                        .asBitmap()
                        .load(imageUrl)
                        .apply(mOptions)
                        .into(holder.item_assetImage);
            }
        }

        if (mSelectPos == position) {
            holder.item_assetShadow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.fx_item_radius_shape_select));
            holder.item_assetName.setTextColor(ContextCompat.getColor(mContext, R.color.ms994a90e2));
        } else {
            holder.item_assetShadow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.fx_item_radius_shape_unselect));
            holder.item_assetName.setTextColor(ContextCompat.getColor(mContext, R.color.ccffffff));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, position);
                }

                if (mSelectPos == position)
                    return;

                notifyItemChanged(mSelectPos);
                mSelectPos = position;
                notifyItemChanged(mSelectPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilterDataList.size();
    }
}
