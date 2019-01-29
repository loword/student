package com.meishe.paintbrush.utils;

import java.io.File;

/**
 * Created by ms on 2018/10/12.
 */

public class FileManagerUtils {


    /**
     * 判断当前文件是否存在
     *
     * @param strFile
     * @return
     */
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
