package com.supe.stu.video.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.ar.face.FaceArResourcesHelpers;
import com.baidu.ar.util.ARFileUtils;
import com.baidu.ar.util.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyong on 2018/4/12.
 */

public class FacerARAssetsManager {

    private FacerArAssetCallBack mModelCallBack;
    private FacerArAssetCallBack mMaskCallBack;
    private FacerArAssetCallBack mConfigCallBack;

    private Context mContext;
    private File sourceDir = new File("sdcard/facear/");

    private Map<String, Integer> deviceLevelMap = new HashMap();

    public FacerARAssetsManager(Context context) {
        mContext = context;
        if (!sourceDir.exists()) {
            sourceDir.mkdirs();
        }
    }

    private void parsevipList(String jsonStr) {
        try {
            if (null == jsonStr) {
                return;
            }
            JSONObject rootJson = new JSONObject(jsonStr);
            if (rootJson.has("HeavyList")) {
                JSONArray heavyList = rootJson.getJSONArray("HeavyList");
                for (int i = 0; i < heavyList.length(); i++) {
                    deviceLevelMap.put(heavyList.getString(i), 0);
                }

            }

            if (rootJson.has("MediumList")) {
                JSONArray mediumList = rootJson.getJSONArray("MediumList");
                for (int i = 0; i < mediumList.length(); i++) {
                    deviceLevelMap.put(mediumList.getString(i), 1);
                }

            }

            if (rootJson.has("LightList")) {
                JSONArray lightList = rootJson.getJSONArray("LightList");
                for (int i = 0; i < lightList.length(); i++) {
                    deviceLevelMap.put(lightList.getString(i), 2);
                }

            }

            if (rootJson.has("BlackList")) {
                JSONArray blackList = rootJson.getJSONArray("BlackList");
                for (int i = 0; i < blackList.length(); i++) {
                    deviceLevelMap.put(blackList.getString(i), -1);
                }

            }
            Log.e("FaceAssets", deviceLevelMap.size() + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestFacerArModel(FacerArAssetCallBack callBack) {
        mModelCallBack = callBack;
        AsyncTask<Void, Void, String> bgTask = new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                // 1、获取viplist 并解析
                File vipListDir = new File(sourceDir, AssetsReplaceHelper.DEVICE_VIP_LIST);
                ARFileUtils.deleteDir(vipListDir);
                try {
                    FaceArResourcesHelpers.extractAssetsRecursive(AssetsReplaceHelper.DEVICE_VIP_LIST, sourceDir,
                            mContext.getAssets(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 解析 vip_list
                parsevipList(FileUtils.readFileText(vipListDir));
                String modelPath = "";
                try {
                    FaceArResourcesHelpers.extractAssetsRecursive("Models", sourceDir,
                            mContext.getAssets(), false);
                    File modelDir = new File(sourceDir, "Models");
                    String modelName;
                    switch (choiceTrackModelByCpu()) {
                        case -1:
                            modelName = "";
                            break;
                        case 0:
                            modelName = "S45150 LiveDriver BaiduAndroidHeavyAutoCalibration.imbin";
                            break;
                        case 1:
                            modelName = "S45150 LiveDriver BaiduAndroidMediumAutoCalibration.imbin";
                            break;
                        case 2:
                            modelName = "S45150 LiveDriver BaiduAndroidLightAutoCalibration.imbin";
                            break;
                        default:
                            modelName = "S45150 LiveDriver BaiduAndroidLightAutoCalibration.imbin";
                    }
                    if (!TextUtils.isEmpty(modelName)) {
                        File modelFile = new File(modelDir, modelName);
                        modelPath = modelFile.getAbsolutePath();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return modelPath;
            }

            protected void onPostExecute(String params) {
                if (mModelCallBack != null) {
                    mModelCallBack.onResult(params);
                }
            }
        };

        bgTask.execute((Void) null);
    }

    public void requestFacerArMask(FacerArAssetCallBack callBack) {
        mMaskCallBack = callBack;
        AssetsReplaceHelper.getInstance().setRootDir(sourceDir);
        AsyncTask<Void, Void, String> bgTask = new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                File masksDir = new File(sourceDir, "Masks");
                //                if (AssetsReplaceHelper.getInstance().isReplaceResource(mContext.getAssets(),
                //                        AssetsReplaceHelper.MASK_CONFIG_NAME,
                //                        AssetsReplaceHelper.FACE_UPDATE_CONFIG_FIELD_MASK)) {
                File maskDir = new File(masksDir.getAbsolutePath());
                //                    if (maskDir.exists()) {
                ARFileUtils.deleteDir(maskDir);
                //                    }
                try {
                    FaceArResourcesHelpers.extractAssetsRecursive(AssetsReplaceHelper.MASK_CONFIG_NAME, sourceDir,
                            mContext.getAssets(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                extraAssets(sourceDir, "Masks");
                //                }

                return masksDir.getAbsolutePath();
            }

            protected void onPostExecute(String params) {
                if (mMaskCallBack != null) {
                    mMaskCallBack.onResult(params);
                }
            }
        };

        bgTask.execute((Void) null);
    }

    public void requestFacerArDeviceVipList(FacerArAssetCallBack callBack) {
        mMaskCallBack = callBack;
        AssetsReplaceHelper.getInstance().setRootDir(sourceDir);
        AsyncTask<Void, Void, String> bgTask = new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                //                if (AssetsReplaceHelper.getInstance().isReplaceResource(mContext.getAssets(),
                //                        AssetsReplaceHelper.MASK_CONFIG_NAME,
                //                        AssetsReplaceHelper.FACE_UPDATE_CONFIG_FIELD_MASK)) {
                File vipListDir = new File(sourceDir, AssetsReplaceHelper.DEVICE_VIP_LIST);
                //                    if (maskDir.exists()) {
                ARFileUtils.deleteDir(vipListDir);
                //                    }
                try {
                    FaceArResourcesHelpers.extractAssetsRecursive(AssetsReplaceHelper.DEVICE_VIP_LIST, sourceDir,
                            mContext.getAssets(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return vipListDir.getAbsolutePath();
            }

            protected void onPostExecute(String params) {
                if (mMaskCallBack != null) {
                    mMaskCallBack.onResult(params);
                }
            }
        };

        bgTask.execute((Void) null);
    }

    public void requestFacerArConfig(FacerArAssetCallBack callBack) {
        mConfigCallBack = callBack;
        AssetsReplaceHelper.getInstance().setRootDir(sourceDir);
        AsyncTask<Void, Void, String> bgTask = new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                File masksDir = new File(sourceDir, "face");

                //                if (AssetsReplaceHelper.getInstance().isReplaceResource(mContext.getAssets(),
                //                        AssetsReplaceHelper.FACE_CONFIG_NAME,
                //                        AssetsReplaceHelper.FACE_UPDATE_CONFIG_FIELD_RES)) {
                File maskDir = new File(masksDir.getAbsolutePath());
                //                    if (maskDir.exists()) {
                ARFileUtils.deleteDir(maskDir);
                //                    }
                try {
                    FaceArResourcesHelpers.extractAssetsRecursive(AssetsReplaceHelper.FACE_CONFIG_NAME, sourceDir,
                            mContext.getAssets(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                extraAssets(sourceDir, "face");
                //                }

                return masksDir.getAbsolutePath();
            }

            protected void onPostExecute(String params) {
                if (mConfigCallBack != null) {
                    mConfigCallBack.onResult(params);
                }
            }
        };

        bgTask.execute((Void) null);
    }

    private String extraAssets(File cacheDir, String dirName) {
        String extraOutPath = null;
        try {
            FaceArResourcesHelpers.extractAssetsRecursive(dirName, cacheDir,
                    mContext.getAssets(), true);
            File masksfile = new File(cacheDir, dirName);
            if (masksfile.exists()) {
                extraOutPath = masksfile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extraOutPath;
    }

    // 如果vip_list 为空的情况 会初始化本地的列表
    private void initDefaultVipList() {
        Log.e("initDefaultVipList", "vip -list empty");
        // 0 高端机  1 终端机  2低端机 -1建议加入黑名单
        deviceLevelMap.put("kirin970", 0); // mate 10 pro v10 p20

        deviceLevelMap.put("Qualcomm Technologies, Inc MSM8998", 1); // 三星s8 小米6 小米 MIX2
        deviceLevelMap.put("Hisilicon Kirin 955", 1);
        deviceLevelMap.put("Qualcomm Technologies, Inc SDM660", 1); // oppo r11s

        deviceLevelMap.put("hi3650", 1); // mate 8
        deviceLevelMap.put("sailfish", 1); // pixel
        deviceLevelMap.put("kirin960", 1); // mate 9

        deviceLevelMap.put("Qualcomm Technologies, Inc MSM8996", 2); //  三星s7

        deviceLevelMap.put("mt6797", -1); // 红米 note4
        deviceLevelMap.put("hi3630", -1); // 荣耀6p
        deviceLevelMap.put("Hisilicon Kirin 935", -1);
        deviceLevelMap.put("Qualcomm Technologies, Inc MSM8994", -1); // google nexus 6 plus
    }

    private Map<String, String> getCPUInfo() {
        try {

            Map<String, String> output = new HashMap<>();

            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));

            String str;

            while ((str = br.readLine()) != null) {
                String[] data = str.split(":");

                if (data.length > 1) {

                    String key = data[0].trim().replace(" ", "_");
                    if (key.equals("model_name")) {
                        key = "cpu_model";
                    }

                    String value = data[1].trim();

                    if (key.equals("cpu_model")) {
                        value = value.replaceAll("\\s+", " ");
                    }

                    output.put(key, value);

                }
            }
            br.close();
            if (!output.keySet().contains("Hardware")) {
                output.put("Hardware", android.os.Build.HARDWARE);
            }

            Log.e("DEVICE_INFO", output.get("Hardware"));

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int choiceTrackModelByCpu() {
        if (deviceLevelMap.size() <= 0) {
            initDefaultVipList();
        }
        Map<String, String> map = getCPUInfo();
        String cpuName = map.get("Hardware");
        if (!TextUtils.isEmpty(cpuName)) {
            if (deviceLevelMap.containsKey(cpuName)) {
                int level = deviceLevelMap.get(cpuName);
                return level;
            }
        }
        return -1;
    }

    /**
     * assets资源写入SD卡回调接口
     */
    public interface FacerArAssetCallBack {

        /**
         * 模型资源写入回调
         *
         * @param modelPath 下载结果
         */
        void onResult(String modelPath);
    }
}

