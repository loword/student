package com.meishe.arface2;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.meishe.arface2.R;

import java.util.ArrayList;



public class ItemSelectAdapter extends RecyclerView.Adapter<ItemSelectAdapter.ItemViewHolder>{

    public static final int[] Daoju_RES_ARRAY = {R.mipmap.ic_delete_all, R.mipmap.diss, R.mipmap.hdj, R.mipmap.glassfour};

    public static final int[] FILTER_RES_ARRAY = {
            R.mipmap.ic_delete_all, R.mipmap.sage, R.mipmap.maid, R.mipmap.mace, R.mipmap.lace,
            R.mipmap.mall, R.mipmap.sap, R.mipmap.sara, R.mipmap.pinky, R.mipmap.sweet, R.mipmap.fresh,
    };

    public static ArrayList FILTERS_NAME = new ArrayList();
    public static ArrayList STICER_NAME = new ArrayList();

    public static final int RECYCLEVIEW_TYPE_EFFECT = 0;
    public static final int RECYCLEVIEW_TYPE_FILTER = 1;

    private RecyclerView mOwnerRecyclerView;
    private int mOwnerRecyclerViewType;

    private final int EFFECT_DEFAULT_CLICK_POSITION = 1;
    private final int FILTER_DEFAULT_CLICK_POSITION = 0;
    private int m_selectedPos;
    private OnItemSelectedListener mOnItemSelectedListener;

    public ItemSelectAdapter(RecyclerView recyclerView, int recyclerViewType) {
        mOwnerRecyclerView = recyclerView;
        mOwnerRecyclerViewType = recyclerViewType;
        if(mOwnerRecyclerViewType == RECYCLEVIEW_TYPE_EFFECT){
            m_selectedPos = EFFECT_DEFAULT_CLICK_POSITION;
        }else if(mOwnerRecyclerViewType == RECYCLEVIEW_TYPE_FILTER){
            m_selectedPos = FILTER_DEFAULT_CLICK_POSITION;
        }
    }

    @Override
    public int getItemCount() {
        return mOwnerRecyclerViewType == RECYCLEVIEW_TYPE_EFFECT ?
                STICER_NAME.size() :
                FILTERS_NAME.size();
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (m_selectedPos == position) {
            holder.mItemView.setSelectedBackground();
        } else {
            holder.mItemView.setUnselectedBackground();
        }

        if (mOwnerRecyclerViewType == RECYCLEVIEW_TYPE_EFFECT) {
            if(position < Daoju_RES_ARRAY.length) {
                holder.mItemView.setItemIcon(Daoju_RES_ARRAY[position]);
            } else {
                holder.mItemView.setItemIcon(R.mipmap.default_face);
            }
        } else {
            holder.mItemView.setItemIcon(FILTER_RES_ARRAY[position % FILTER_RES_ARRAY.length]);
            holder.mItemView.setItemText(FILTERS_NAME.get(position % FILTERS_NAME.size()).toString());
        }

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mOnItemSelectedListener != null){
                    mOnItemSelectedListener.onItemSelected(position);
                }

                notifyItemChanged(m_selectedPos);
                m_selectedPos = position;
                notifyItemChanged(m_selectedPos);
            }
        });
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(new ItemView(parent.getContext(), mOwnerRecyclerViewType));
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemView mItemView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (ItemView) itemView;
        }
    }


    public interface OnItemSelectedListener {
        void onItemSelected(int itemPosition);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }
}
