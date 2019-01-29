package com.meishe.areffect;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class ItemView extends LinearLayout{
    private ImageView mItemIcon;
    private TextView mItemText;
    private Context mContext;

    private int mItemType;//effect or filter

    public ItemView(Context context, int itemType) {
        super(context);
        this.mItemType = itemType;
        mContext = context;
        init(context);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void init(Context context) {
        View viewRoot = LayoutInflater.from(context).inflate(R.layout.item_view,
                this, true);
        mItemIcon = (ImageView) viewRoot.findViewById(R.id.item_icon);
        mItemText = (TextView) viewRoot.findViewById(R.id.item_text);
        if (mItemType == ItemSelectAdapter.RECYCLEVIEW_TYPE_FILTER) {
            mItemText.setVisibility(VISIBLE);
        }
    }

    public void setUnselectedBackground() {
        mItemIcon.setBackground(getResources().getDrawable(R.drawable.circle_unselected));
        if (mItemType == ItemSelectAdapter.RECYCLEVIEW_TYPE_FILTER) {
            mItemText.setTextColor(Color.parseColor("#575757"));
            mItemIcon.setPadding(0, 0, 0, 0);
        }
    }

    public void setSelectedBackground() {
        mItemIcon.setBackground(getResources().getDrawable(R.drawable.circle_selected));
        if (mItemType == ItemSelectAdapter.RECYCLEVIEW_TYPE_FILTER) {
            mItemText.setTextColor(Color.parseColor("#3db5fe"));
            int pading = dip2px(mContext, 2);
            mItemIcon.setPadding(pading, pading, pading, pading);
        }
    }

    public void setItemIcon(int resourceId) {
        mItemIcon.setImageDrawable(getResources().getDrawable(resourceId));
    }

    public void setItemText(String text) {
        mItemText.setText(text);
    }
}
