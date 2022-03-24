package com.hikdsj.hikdsj.utils;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.hikdsj.hikdsj.ApiService;
import com.hikdsj.hikdsj.base.Constant;
import com.hikdsj.hikdsj.bean.VideoBean;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.throwable.HttpThrowable;
import com.ljy.devring.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/********************************************************************
 @version: 1.0.0
 @description: 定时任务--StartService中进行时间记录   每30分钟进行一次路径查询，若含有未上传成功的视频文件则进行上传
 @author: 杨帆
 @time: 2022-03-21 14:56
 @变更历史:
 ********************************************************************/
public class UpFileUtils {
    private static final String TAG = Constant.TAG;
    private OnUpFileListener mOnUpFileListener;

    private static volatile UpFileUtils instance;

    private UpFileUtils() {
    }

    public static UpFileUtils getInstance() {
        if (instance == null) {
            synchronized (UpFileUtils.class) {
                if (instance == null) {
                    instance = new UpFileUtils();
                }
            }
        }
        return instance;
    }

    public void setOnUpFileListener(OnUpFileListener onUpFileListener) {
        mOnUpFileListener = onUpFileListener;
    }

    public void toCheckFile() {
        String file_path = haveFile();
        if (!TextUtils.isEmpty(file_path)) {
            goUpVideo(file_path);
            if (mOnUpFileListener != null) {
                mOnUpFileListener.onStartUpListener();
            }
        } else if (mOnUpFileListener != null) {
            Log.e(TAG, "toCheckFile: 没有文件");
            mOnUpFileListener.onFinishListener();
        }
    }

    private void goUpVideo(String path) {
        final File video = new File(path);
        String names = video.getName();
        RequestBody requestFile = RequestBody.create(MediaType.parse(guessMimeType(video.getPath())), video);
        MultipartBody.Part body = null;
        try {
            body = MultipartBody.Part.createFormData("upvideoFile", URLEncoder.encode(names, "UTF-8"), requestFile);
        } catch (UnsupportedEncodingException e) {
            Log.e("ManOneFragment", "toAddClient: 文件名异常" + names + e.toString());
        }
        Log.e(TAG, "goUpVideo: 上传文件" + names);
        DevRing.httpManager().commonRequest(DevRing.httpManager().getService(ApiService.class).upLoadFile(body), new CommonObserver<JSONObject>() {
            @Override
            public void onResult(com.alibaba.fastjson.JSONObject result) {
                VideoBean bean = new Gson().fromJson(result.toString(), VideoBean.class);
                if (bean.getCode() == 200) {
                    FileUtil.deleteFile(video, true);
                }
                String file_path = haveFile();
                if (!TextUtils.isEmpty(file_path)) {
                    goUpVideo(file_path);
                } else if (mOnUpFileListener != null) {
                    mOnUpFileListener.onFinishListener();
                    Log.e(TAG, "toCheckFile: 没有文件");
                }
                Log.d(TAG, result.toJSONString());
            }

            @Override
            public void onError(HttpThrowable throwable) {
                Log.e(TAG, "onError: " + throwable.toString());
                if (mOnUpFileListener != null) {
                    mOnUpFileListener.onFinishListener();
                    Log.e(TAG, "toCheckFile: 没有文件");
                }
            }
        }, TAG);
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    /*是否有文件*/
    private String haveFile() {
        File file = FileUtil.getFile(Constant.VIDEO_FILEP);
        if (file != null) {
            File[] files = file.listFiles();
            int length = files.length;
            if (length != 0) {
                String path = files[length - 1].getPath();
                if (path.contains("start")) {
                    return "";
                } else if (path.contains("Police")) {
                    fileMove(files[length - 1]);
                    return "";
                }
                return files[length - 1].getPath();
            }
        }
        return "";
    }

    /*转移可能存在问题的视频*/
    private void fileMove(File file) {
        try {
            File out_file = FileUtil.getFile("/storage/sdcard0/filecard/");
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(out_file + file.getName());
            int n = 0;
            byte[] b = new byte[1024];
            while ((n = fis.read(b)) != -1) {
                fos.write(b);
                fos.write(b, 0, n);
            }
            fis.close();
            fos.close();
            Log.i(TAG, "转移成功");
            FileUtil.deleteFile(file, true);
        } catch (Exception e) {
            Log.e(TAG, "fileMove: " + e.toString());
        }
    }

    public interface OnUpFileListener {
        void onStartUpListener();

        void onFinishListener();
    }

}
