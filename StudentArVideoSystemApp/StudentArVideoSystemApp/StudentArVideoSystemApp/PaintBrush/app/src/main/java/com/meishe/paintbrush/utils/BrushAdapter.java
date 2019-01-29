package com.meishe.paintbrush.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.meishe.paintbrush.BrushColor;
import com.meishe.paintbrush.BrushStyle;
import com.meishe.paintbrush.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BrushAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "BrushAdapter";
    private Context m_context;
    private int m_type = Constants.ITEM_TYPE_BRUSH_STYLE;
    private OnSelectStyleListener m_onSelectStyleListener;
    private OnSelectColorListener m_onSelectColorListener;
    private List<BrushStyle> m_brushStyleList = new ArrayList<>();
    private List<BrushColor> m_brushColorList = new ArrayList<>();
    private int mSelectStylePos = 0;
    private int mColorSelectedPos = 0;

    public BrushAdapter(Context context, int item_type) {
        m_context = context;
        m_type = item_type;
    }

    public void updateBrushColorData(Map<Integer, BrushColor> brushColorMap) {
        if(brushColorMap == null || brushColorMap.isEmpty()) {
            return;
        }
        List<BrushColor> brushColors = new ArrayList<>(brushColorMap.values());
        m_brushColorList = brushColors;
        notifyDataSetChanged();
    }

    public void updateBrushStyleData(Map<Integer, BrushStyle> brushStyleMap) {
        if(brushStyleMap == null || brushStyleMap.isEmpty()) {
            return;
        }
        List<BrushStyle> brushStyles = new ArrayList<>(brushStyleMap.values());
        m_brushStyleList = brushStyles;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(m_type == Constants.ITEM_TYPE_BRUSH_COLOR) {
            return m_brushColorList.size();
        } else if(m_type == Constants.ITEM_TYPE_BRUSH_STYLE) {
            return m_brushStyleList.size();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof ViewHolderStyle) {
            if(position >= m_brushStyleList.size()) {
                return;
            }
            final BrushStyle styleData = m_brushStyleList.get(position);
            if(styleData == null) {
                return;
            }
            final ViewHolderStyle holderStyle = (ViewHolderStyle) holder;
            holderStyle.m_styleView.setBackground(styleData.drawable);
            if(mSelectStylePos==position){
                holderStyle.mSelecteItem.setVisibility(View.VISIBLE);
            }else {
                holderStyle.mSelecteItem.setVisibility(View.GONE);
            }
            holderStyle.m_styleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(m_onSelectStyleListener != null) {
                        mSelectStylePos = position;
                        notifyDataSetChanged();
                        BrushStyle data = styleData.clone();
                        m_onSelectStyleListener.onItemSelected(position, data);
                    }
                }
            });
        } else if(holder instanceof ViewHolderColor ) {
            if(position >= m_brushColorList.size()) {
                return;
            }
            final BrushColor colorData = m_brushColorList.get(position);
            if(colorData == null) {
                return;
            }
            ViewHolderColor holderColor = (ViewHolderColor) holder;
            holderColor.m_colorView.setColor(colorData.color);
            if(mColorSelectedPos==position){
                holderColor.mSelecteItem.setVisibility(View.VISIBLE);
            }else {
                holderColor.mSelecteItem.setVisibility(View.GONE);
            }
            holderColor.m_colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(m_onSelectColorListener != null) {
                        mColorSelectedPos = position;
                        BrushColor data = colorData.clone();
                        notifyDataSetChanged();
                        m_onSelectColorListener.onItemSelected(position, data);
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(m_type == Constants.ITEM_TYPE_BRUSH_COLOR) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nv_item_brush_color, parent, false);
            return new ViewHolderColor(v);
        } else if(m_type == Constants.ITEM_TYPE_BRUSH_STYLE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nv_item_brush_style, parent, false);
            return new ViewHolderStyle(v);
        }
        return null;
    }

    class ViewHolderStyle extends RecyclerView.ViewHolder {
        private ImageButton m_styleView;
        View mSelecteItem;
        public ViewHolderStyle(View itemView) {
            super(itemView);
            m_styleView = (ImageButton) itemView.findViewById(R.id.brush_style);
            mSelecteItem = itemView.findViewById(R.id.selectedStyleItem);
        }
    }

    class ViewHolderColor extends RecyclerView.ViewHolder {
        private RoundColorView m_colorView;
        View mSelecteItem;
        public ViewHolderColor(View itemView) {
            super(itemView);
            m_colorView = (RoundColorView) itemView.findViewById(R.id.round_color_view);
            mSelecteItem = itemView.findViewById(R.id.selectedItem);
        }
    }

    public interface OnSelectStyleListener {
        void onItemSelected(int position, BrushStyle itemData);
    }
    public interface OnSelectColorListener {
        void onItemSelected(int position, BrushColor itemData);
    }

    public void setOnSelectStyleListener(OnSelectStyleListener onSelectStyleListener) {
        this.m_onSelectStyleListener = onSelectStyleListener;
    }
    public void setOnSelectColorListener(OnSelectColorListener onSelectColorListener) {
        this.m_onSelectColorListener = onSelectColorListener;
    }
}

