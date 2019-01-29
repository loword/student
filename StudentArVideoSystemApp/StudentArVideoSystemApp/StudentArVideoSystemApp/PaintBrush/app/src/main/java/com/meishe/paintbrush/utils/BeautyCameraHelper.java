package com.meishe.paintbrush.utils;

import com.meishe.paintbrush.MSApplication;

import java.io.File;

public class BeautyCameraHelper {

    public static String getRootpath(){
        return MSApplication.getmContext().getExternalCacheDir()+ File.separator;
    }
}
