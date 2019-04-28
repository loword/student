package com.meishe.sdkdemo.edit.data;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by admin on 2018/11/28.
 */
public class ParseJsonFile{
    /**
     * Json转Java对象
     */
    public static <T> T fromJson(String json, Class<T> clz) {
        return new Gson().fromJson(json, clz);
    }

    public static ArrayList<FxJsonFileInfo.JsonFileInfo> readBundleFxJsonFile(Context context,String bundleJsonFilePath){
        if(context == null)
            return null;
        if(TextUtils.isEmpty(bundleJsonFilePath))
            return null;

        try {
            InputStream inputStream = context.getAssets().open(bundleJsonFilePath);
            if(inputStream == null)
                return null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            String infoStrLine;
            StringBuilder strbuilder = new StringBuilder();
            while ((infoStrLine = bufferedReader.readLine()) != null) {
                strbuilder.append(infoStrLine);
            }
            bufferedReader.close();
            FxJsonFileInfo resultInfo = fromJson(strbuilder.toString(), FxJsonFileInfo.class);
            ArrayList<FxJsonFileInfo.JsonFileInfo> infoLists = resultInfo.getFxInfoList();
            return infoLists;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class FxJsonFileInfo {

        public ArrayList<JsonFileInfo> getFxInfoList() {
            return fxInfoList;
        }

        private ArrayList<JsonFileInfo> fxInfoList;
        public static class JsonFileInfo {
            public String getName() {
                return name;
            }

            private String name;//素材名字

            public String getFxPackageId() {
                return fxPackageId;
            }

            private String fxPackageId;//素材包Id

            public String getFxFileName() {
                return fxFileName;
            }

            private String fxFileName;//素材特效包文件名

            public String getFxLicFileName() {
                return fxLicFileName;
            }

            private String fxLicFileName;//素材特效包授权文件名

            public String getImageName() {
                return imageName;
            }

            private String imageName;//素材封面

            public String getFitRatio() {
                return fitRatio;
            }

            private String fitRatio;//适配比例，参考NvAsset的定义
        }
    }
}
