package com.meishe.paintbrush.utils;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class PathUtil {
    public static final int RESOURCE_TYPE_MUSICIMAGE = 0;
    public static final int RESOURCE_TYPE_MUSICFILE = 1;
    public static final int RESOURCE_TYPE_STICKERIMAGE = 2;
    public static final int RESOURCE_TYPE_STICKERFILE = 3;
    public static final int RESOURCE_TYPE_FUIMAGE = 4;
    public static final int RESOURCE_TYPE_FUFILE = 5;
    public static final int RESOURCE_TYPE_ANGLEIMAGE = 6;
    public static final int RESOURCE_TYPE_Zip = 7;
    public static final int RESOURCE_TYPE_EDITSTICKERFILE = 9;
    public static final int RESOURCE_TYPE_EDITSTICKERIMAGE = 10;


    private static final String TAG = PathUtil.class.getName();
    private static final String rootpath = BeautyCameraHelper.getRootpath();
    //private static final String downloadRootPath = PlatformConfig.getAppContext().getExternalCacheDir() + File.separator + ".NvStreamingSdk" + File.separator + "cdv" + File.separator + "Download";
    private static final String downloadRootPath =  rootpath + "NvStreamingSdk" + File.separator + "cdv" + File.separator + "Download";
    private static String DRAFT_DIRECTORY = rootpath + ".NvStreamingSdk" + File.separator + "cdv" + File.separator + "Draft";
    private static String RECORD_DIRECTORY = ".NvStreamingSdk" + File.separator + "cdv" + File.separator + "Record";
    private static String COMPILE_DIRECTORY = ".NvStreamingSdk" + File.separator + "cdv" + File.separator + "Compile";
    private static String CLIP_IMAGE_PATH = ".NvStreamingSdk" + File.separator + "cdv" + File.separator + "cover";
    private static String PhOTO_IMAGE_PATH = "bohe" + File.separator  + "cdv";


    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }

    }

    /**
     * 获取下载文件路径名
     * @param resType  文件类型
     * @param resId  文件名称
     * @return  完整文件路径名
     */
    public static String getDownloadFullName(int resType, String resId) {
        String fullPath = null;
        String dir = getDownloadDestDir(resType);
        File file = new File(dir);
        if (!checkDownloadDir(file)) {
            Log.e(TAG, "获取下载路径失败");
            return null;
        }

        switch (resType) {
            case RESOURCE_TYPE_MUSICIMAGE:
                fullPath = dir + File.separator + resId + ".jpg";
                break;
            case RESOURCE_TYPE_ANGLEIMAGE:
                fullPath = dir + File.separator + resId + ".jpg";
                break;
            case RESOURCE_TYPE_MUSICFILE:
                fullPath = dir + File.separator + resId + ".mp3";
                break;
            case RESOURCE_TYPE_STICKERIMAGE:
                fullPath = dir + File.separator + resId + ".jpg";
                break;
            case RESOURCE_TYPE_STICKERFILE:
                fullPath = dir + File.separator + resId + ".zip";
                break;
            case RESOURCE_TYPE_FUIMAGE:
                fullPath = dir + File.separator + resId + ".jpg";
                break;
            case RESOURCE_TYPE_FUFILE:
                fullPath = dir + File.separator + resId + ".asset";
                break;
            case RESOURCE_TYPE_Zip:
                fullPath = dir + File.separator + resId + ".zip";
                break;

            case RESOURCE_TYPE_EDITSTICKERFILE:
                fullPath = dir + File.separator + resId + ".animatedsticker";
                break;

            case RESOURCE_TYPE_EDITSTICKERIMAGE:
                fullPath = dir + File.separator + resId + ".jpg";
                break;

            default:
                break;
        }

        return fullPath;
    }

    /**
     *  获取文件下载目录
     * @param resType  类型
     * @return  目录名称
     */
    public static String getDownloadDestDir(int resType) {
        String destPath = null;
        switch (resType) {
            case RESOURCE_TYPE_MUSICIMAGE:
                destPath = downloadRootPath + File.separator + "Music" + File.separator + "Image";
                break;
            case RESOURCE_TYPE_ANGLEIMAGE:
                destPath = downloadRootPath + File.separator + "Angle" + File.separator + "Image";
                break;
            case RESOURCE_TYPE_MUSICFILE:
                destPath = downloadRootPath + File.separator + "Music" + File.separator + "File";
                break;
            case RESOURCE_TYPE_STICKERIMAGE:
                destPath = downloadRootPath + File.separator + "Sticker" + File.separator + "Image";
                break;
            case RESOURCE_TYPE_STICKERFILE:
                destPath = downloadRootPath + File.separator + "Sticker" + File.separator + "File";
                break;
            case RESOURCE_TYPE_FUIMAGE:
                destPath = downloadRootPath + File.separator + "Fu" + File.separator + "Image";
                break;
            case RESOURCE_TYPE_FUFILE:
                destPath = downloadRootPath + File.separator + "Fu" + File.separator + "File";
                break;
            case RESOURCE_TYPE_Zip:
                destPath = downloadRootPath + File.separator + "Props" + File.separator + "File";
                break;

            case RESOURCE_TYPE_EDITSTICKERFILE:
                destPath = downloadRootPath + File.separator + "EditSticker" + File.separator + "File";
                break;
            case RESOURCE_TYPE_EDITSTICKERIMAGE:
                destPath = downloadRootPath + File.separator + "EditSticker" + File.separator + "Image";
                break;

            default:
                break;
        }

        return destPath;
    }

    /**
     *  检查文件是否存在
     * @param file  文件名
     * @return  文件是否存在
     */
    private static boolean checkDownloadDir(File file) {
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "Failed to make Download directory");
            return false;
        }
        return true;
    }

    /**
     *   解压缩zip文件
     * @param zipFile   需要解压缩的zip文件
     * @param targetDir  目标目录
     */
    public static void Unzip(String zipFile, String targetDir) {
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String strEntry; //保存每个zip的条目名称
        try {
            BufferedOutputStream dest = null; //缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; //每个zip条目的实例
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    Log.i("Unzip: ", "=" + entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();
                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
    }

    /**
     *   获取解压缩目录
     * @param zipFile  压缩文件
     * @return  路径名
     */
    public static String getUnZipPath(String zipFile) {
        if (zipFile == null) {
            return null;
        }
        String filePath = null;
        String fileName = getFileName(zipFile);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        return filePath;
    }

    /**
     *   获取文件名，去除后缀以及路径
     * @param pathandname  文件名
     * @return  去除后缀的文件名
     */
    public static String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }

    /**
     *   获取生成视频的目录
     * @return  目录名
     */

    public static String getCompileDirectory() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), COMPILE_DIRECTORY);
        if (!captureDir.exists()) {
            return null;
        } else {
            return captureDir.getAbsolutePath();
        }

    }
    /**
     *   获取图片裁剪的目录
     * @return  目录名
     */

    public static String getCoverImageDirectory() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), CLIP_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make image directory");
            return null;
        } else {
            return captureDir.getAbsolutePath();
        }

    }
    /**
     *   获取webp的文件名
     * @return  webp的文件名
     */
    public static String getWebPFilePath() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), CLIP_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".webp";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }
    /**
     *   获取图片裁剪的文件名
     * @return  图片裁剪的文件名
     */
    public static String getCoverImageFilePath() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), CLIP_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".jpg";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }
    /**
     *   获取视频画笔生成视频的文件名
     * @return  视频画笔生成视频的文件名
     */
    public static String getCoverVideoFilePath() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), CLIP_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory mp4");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".mp4";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }
    /**
     *   获取图片的文件名
     * @return  图片的文件名
     */
    public static String getPhotoImageFilePath() {
        File captureDir = new File(Environment.getExternalStorageDirectory(), PhOTO_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".jpg";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }
    /**
     *   获取图片的目录
     * @return  图片目录名
     */

    public static String getPhotoImageDirectory() {
        File captureDir = new File(Environment.getExternalStorageDirectory(), PhOTO_IMAGE_PATH);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }
        return captureDir.getAbsolutePath();
    }
    /**
     *   获取生成视频的视频名称
     * @return  生成视频的视频名称
     */
    public static String getCompileFilePath() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), COMPILE_DIRECTORY);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".mp4";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }

    public static String getRecordDirectory() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), RECORD_DIRECTORY);
        if (!captureDir.exists()) {
            return null;
        } else {
            return captureDir.getAbsolutePath();
        }

    }
    /**
     *   获取拍摄视频的视频名称
     * @return  拍摄视频的视频名称
     */
    public static String getRecordFilePath() {
        File captureDir = new File(BeautyCameraHelper.getRootpath(), RECORD_DIRECTORY);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

        String filename = Util.getCharacterAndNumber() + ".mp4";

        File file = new File(captureDir, filename);
        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();
    }

    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
        }
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static String getFileByExtension(String Path, String Extension) {
        File[] files = new File(Path).listFiles();
        if (files == null) {
            return null;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                {
                    return f.getPath();
                }
            }
        }

        return null;
    }

    public static boolean checkFileExists(Activity activity, String fileName) {
        String path = PathUtil.getDraftFilePath(activity, fileName);
        if (path == null) {
            Log.d("meishe", "checkFileExists    draft path is null!");
            return false;
        }
        File file = new File(path);
        return file.exists();
    }
    /**
     *   获取缓存文件的目录
     * @return  缓存文件的目录名
     */

    public static String getDraftFilePath(Context activity, String filename) {
        if (activity == null) {
            return null;
        }
        File captureDir = new File(activity.getFilesDir(), DRAFT_DIRECTORY);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

//        String filename = "draft.json";

        File file = new File(captureDir, filename);
        return file.getAbsolutePath();
    }
    public static String getDraftFilePath(Activity activity, String filename) {
        if (activity == null) {
            return null;
        }
        File captureDir = new File(activity.getFilesDir(), DRAFT_DIRECTORY);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            Log.e(TAG, "Failed to make NetEase directory");
            return "";
        }

//        String filename = "draft.json";

        File file = new File(captureDir, filename);
        return file.getAbsolutePath();
    }

    public static List<String> getAllFiles(String path) {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files==null || files.length == 0) {
                return list;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        getAllFiles(file2.getAbsolutePath());
                    } else {
                        list.add(file2.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        return list;
    }

    /**
     *   解压自拍文件
     * @param zipFile  zip文件
     * @param folderPath  目标文件名
     * @return  是否解压成功
     */
    public static boolean unZipFile(String zipFile, String folderPath) {
        ZipFile zfile = null;
        try {
            // 转码为GBK格式，支持中文
            zfile = new ZipFile(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            // 列举的压缩文件里面的各个文件，判断是否为目录
            if (ze.isDirectory()) {
                String dirstr = folderPath +  File.separator + ze.getName();
                dirstr.trim();
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            OutputStream os = null;
            FileOutputStream fos = null;
            // ze.getName()会返回 script/start.script这样的，是为了返回实体的File
            File realFile = getRealFileName(folderPath, ze.getName());
            try {
                fos = new FileOutputStream(realFile);
            } catch (FileNotFoundException e) {
                return false;
            }
            os = new BufferedOutputStream(fos);
            InputStream is = null;
            try {
                is = new BufferedInputStream(zfile.getInputStream(ze));
            } catch (IOException e) {
                return false;
            }
            int readLen = 0;
            // 进行一些内容复制操作
            try {
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    os.write(buf, 0, readLen);
                }
            } catch (IOException e) {
                return false;
            }
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                return false;
            }
        }
        try {
            zfile.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static File getRealFileName(String baseDir, String absFileName) {
        absFileName = absFileName.replace("\\", "/");
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                ret = new File(ret, substr);
            }

            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            ret = new File(ret, substr);
            return ret;
        } else {
            ret = new File(ret, absFileName);
        }
        return ret;
    }
}
