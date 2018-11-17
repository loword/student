package com.supe.stu.video.utils;

import android.content.res.AssetManager;

import com.baidu.ar.util.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

/**
 * Created by baidu on 2018/5/8.
 */

public class AssetsReplaceHelper {

    private File cacheDir;

    private static AssetsReplaceHelper sAssetsReplaceHelper;

    public static final String MASK_CONFIG_NAME = "masks_update_config.json";
    public static final String FACE_CONFIG_NAME = "res_update_config.json";
    public static final String DEVICE_VIP_LIST = "vip_list.json";

    public static final String FACE_UPDATE_CONFIG_FIELD_MASK = "face_masks_data";

    public static final String FACE_UPDATE_CONFIG_FIELD_RES = "face_config_data";

    private AssetsReplaceHelper() {

    }

    public static AssetsReplaceHelper getInstance() {
        if (sAssetsReplaceHelper == null) {
            sAssetsReplaceHelper = new AssetsReplaceHelper();
        }
        return sAssetsReplaceHelper;
    }

    public void setRootDir(File rootDir) {
        cacheDir = rootDir;
    }

    public boolean isReplaceResource(AssetManager assetManager, String jsonFileName, String faceParseHead) {
        try {

            InputStream inputStream = assetManager.open(jsonFileName);
            int lenght = inputStream.available();
            byte[] buffer = new byte[lenght];
            inputStream.read(buffer);
            String result = new String(buffer, "utf8");
            int assetsVersion = parseUpdateConfigJson(result, faceParseHead);

            // 读取update_config文件
            File loaclConfigFile = new File(cacheDir, jsonFileName);
            String localConfigStr = FileUtils.readFileText(loaclConfigFile);
            int localVersion = parseUpdateConfigJson(localConfigStr, faceParseHead);

            return !(assetsVersion == localVersion);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private int parseUpdateConfigJson(String json, String faceField) {
        try {

            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(faceField)) {
                JSONObject subObject = jsonObject.getJSONObject(faceField);
                if (subObject.has("version")) {
                    int resourceVersion = subObject.getInt("version");
                    return resourceVersion;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
