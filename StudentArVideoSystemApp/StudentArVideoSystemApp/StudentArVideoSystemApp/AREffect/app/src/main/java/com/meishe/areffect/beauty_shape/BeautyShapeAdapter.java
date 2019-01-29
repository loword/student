package com.meishe.areffect.beauty_shape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meishe.areffect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜列表的adapter
 */
public class BeautyShapeAdapter extends RecyclerView.Adapter<BeautyShapeAdapter.ViewHolder> {
    private Context mContext;
    private List<BeautyShapeListItem> mDataList = new ArrayList<>();
    private OnItemClickListener mListener;

    public BeautyShapeAdapter(Context context, List<BeautyShapeListItem> list) {
        mContext = context;
        mDataList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_beauty_shape, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (mDataList == null || position >= mDataList.size() || position < 0)
            return;

        final BeautyShapeListItem recordFxListItem = mDataList.get(position);
        if (recordFxListItem == null)
            return;

        holder.fx_name.setText(recordFxListItem.nameCH);
        if(recordFxListItem.image_drawable != null) {
            holder.fx_image.setBackground(recordFxListItem.image_drawable);
        }

        if (recordFxListItem.selected) {
            holder.fx_select.setVisibility(View.VISIBLE);
        } else {
            holder.fx_select.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mDataList.get(position).selected) {
                    mDataList.get(position).selected = true;
                    for (int i = 0; i < mDataList.size(); ++i) {
                        if (i != position) {
                            mDataList.get(i).selected = false;
                        }
                    }
                }
                notifyDataSetChanged();

                if (mListener != null) {
                    mListener.fxSelected(position, mDataList.get(position));
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fx_name;
        public ImageView fx_image;
        public ImageView fx_select;

        public ViewHolder(View itemView) {
            super(itemView);
            fx_name = (TextView) itemView.findViewById(R.id.item_text);
            fx_image = (ImageView) itemView.findViewById(R.id.item_icon);
            fx_select = (ImageView) itemView.findViewById(R.id.item_select);
        }
    }

    public interface OnItemClickListener {
        void fxSelected(int pos, BeautyShapeListItem RecordFxListItem);
    }
}
