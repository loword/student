package com.supe.stu.video.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

public class DeviceInfoUtils {

    private static final File BUILD_PROP_FILE = new File(Environment.getRootDirectory(), "build.prop");

    private static Properties getBuildProperties() {
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(BUILD_PROP_FILE);
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public static boolean checkIfARM64() {
        String type = getBuildProperties().getProperty("ro.product.cpu.abi", "arm");
        if (type.contains("arm64")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMIUI() {
        return getBuildProperties().containsKey("ro.miui.ui.version.name");
    }

    public static boolean isHuaWei() {
        return getBuildProperties().containsKey("ro.build.version.emui");
    }

    public static boolean isOppo() {
        return getBuildProperties().containsKey("ro.build.version.opporom");
    }

    public static boolean isVivo() {
        return getBuildProperties().containsKey("ro.vivo.os.version");
    }


    // 获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }
}
