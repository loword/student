package com.me.hiet.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.me.hiet.edit.data.FilterItem;
import com.me.hiet.edit.filter.FilterAdapter;
import com.meishe.sdkdemo.R;

import java.util.ArrayList;

/**
 * Created by admin on 2018/11/15.
 */

public class FaceUPropView extends RelativeLayout {
    private RecyclerView mFaceUPropList;
    private LinearLayout mMoreFaceUProp;
    private ImageButton mMoreFaceUPropImage;
    private TextView mMoreFaceUPropText;
    private FilterAdapter mFaceUPropAdapter;

    private OnFaceUPropListener mFaceUPropListener;
    public interface OnFaceUPropListener{
        void onItmeClick(View v,int position);
        void onMoreFaceUProp();
    }
    public void setFaceUPropListener(OnFaceUPropListener faceUPropListener) {
        this.mFaceUPropListener = faceUPropListener;
    }
    public void setMoreFaceUPropClickable(boolean clickable){
        mMoreFaceUProp.setClickable(clickable);
    }

    public FaceUPropView(Context context){
        this(context, null);
    }
    public FaceUPropView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public void setSelectedPos(int selectedPos) {
        if(mFaceUPropAdapter != null)
            mFaceUPropAdapter.setSelectPos(selectedPos);
    }
    public void setPropDataArrayList(ArrayList<FilterItem> propDataList) {
        if(mFaceUPropAdapter != null)
            mFaceUPropAdapter.setFilterDataList(propDataList);
    }

    public void notifyDataSetChanged(){
        if(mFaceUPropAdapter != null){
            mFaceUPropAdapter.notifyDataSetChanged();
        }
    }
    public void initPropRecyclerView(Context context) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mFaceUPropList.setLayoutManager(linearLayoutManager);
        mFaceUPropList.setAdapter(mFaceUPropAdapter);

        mFaceUPropAdapter.setOnItemClickListener(new FilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mFaceUPropListener != null){
                    mFaceUPropListener.onItmeClick(view,position);
                }
            }
        });
    }
    private void init(Context context){
        mFaceUPropAdapter = new FilterAdapter(context);
        mFaceUPropAdapter.isArface(true);
        View rootView = LayoutInflater.from(context).inflate(R.layout.faceu_prop_view, this);
        mFaceUPropList = (RecyclerView) rootView.findViewById(R.id.faceUPropList);
        mMoreFaceUProp = (LinearLayout) rootView.findViewById(R.id.moreFaceUProp);
        mMoreFaceUProp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFaceUPropListener != null){
                    mFaceUPropListener.onMoreFaceUProp();
                }
            }
        });
        mMoreFaceUPropImage = (ImageButton)rootView.findViewById(R.id.moreFaceUPropImage);
        mMoreFaceUPropImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoreFaceUProp.callOnClick();
            }
        });
        mMoreFaceUPropText = (TextView)rootView.findViewById(R.id.moreFaceUPropText);
        mMoreFaceUPropText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoreFaceUProp.callOnClick();
            }
        });
    }
}
