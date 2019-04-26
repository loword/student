package com.me.hiet.edit.data;

import com.me.hiet.utils.asset.NvAsset;

/**
 * Created by admin on 2018/7/10.
 */

public class AssetItem {
    public static final int ASSET_NONE = 0;//表示无素材
    public static final int ASSET_LOCAL = 1;//下载到本地的素材
    public static final int ASSET_BUILTIN = 2;//内建素材素材

    public AssetItem(){}

    public int getImageRes() {
        return mImageRes;
    }

    public void setImageRes(int imageRes) {
        this.mImageRes = imageRes;
    }

    public int getAssetMode() {
        return mAssetMode;
    }

    public void setAssetMode(int fontMode) {
        this.mAssetMode = fontMode;
    }

    public NvAsset getAsset() {
        return mAsset;
    }

    public void setAsset(NvAsset asset) {
        this.mAsset = asset;
    }

    //滤镜对应颜色值，douyin编辑滤镜。粒子编辑滤镜专用
    public String getFilterColor() {
        return mFilterColor;
    }

    //滤镜对应颜色值，douyin编辑滤镜。粒子编辑滤镜专用
    public void setFilterColor(String filterColor) {
        this.mFilterColor = filterColor;
    }


    private int mImageRes;
    private int mAssetMode;
    private NvAsset mAsset;

    //滤镜对应颜色值，douyin编辑滤镜。粒子编辑滤镜专用
    private String mFilterColor;
}
