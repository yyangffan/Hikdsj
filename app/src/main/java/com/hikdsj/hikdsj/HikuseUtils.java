package com.hikdsj.hikdsj;

import android.content.Context;
import android.util.Log;

import com.hikvision.hiksdk.HikSdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HikuseUtils implements HikSdk.MediaResultCallback{
    private static final String TAG = "HikuseUtils";
    private static volatile HikuseUtils instance;
    private ExecutorService executorService;
    private HikSdk hikSdk;
    private HikSdk.MediaClient mediaClient;
    public static final String video_path = "/storage/sdcard0/carvideo.mp4";
    private int cameraIndex = 0;
    private OnRecordListener mOnRecordListener;

    private HikuseUtils(Context context) {
        executorService = Executors.newCachedThreadPool();
        hikSdk = HikSdk.getInstance(context.getContentResolver());
        mediaClient = hikSdk.getMediaInterface();
        mediaClient.registerMediaResultCallback(this);

    }

    public static HikuseUtils getInstance(Context context) {
        if(instance == null){
            synchronized (HikuseUtils.class){
                if(instance == null){
                    instance =new HikuseUtils(context);
                }
            }
        }
        return instance;
    }

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        mOnRecordListener = onRecordListener;
    }

    public  void stspRecod(boolean start){
        executorService.submit(new RecordRunnable());
//        mediaClient.startRecord(0,0,video_path);
    }
    private class RecordRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "RecordRunnable");
            if (mediaClient != null) {
                //1录像 其他未录像
                mediaClient.setFileTags(1,"JQ12344212412",null);
                int ret = mediaClient.queryRecordStatus(cameraIndex);
                Log.e(TAG, "ret:" + ret);
                if (ret == 0) {
                    if(mOnRecordListener!=null){
                        mOnRecordListener.onstartRecord();
                    }
                    //startRecordAddNotify startRecord区别在于是否有想要有通知栏提示
                    //stopRecordDelNotify  stopRecord 配合前者一起使用
//                    mediaClient.startRecordAddNotify(cameraIndex, HikSdk.STREAM_TYPE_MAIN, "/storage/sdcard0/hello.mp4");
                    mediaClient.startRecordAddNotify(cameraIndex, HikSdk.STREAM_TYPE_MAIN, video_path);
//                    mediaClient.startRecord(cameraIndex, HikSdk.STREAM_TYPE_MAIN, "/sdcard/hello.mp4");
//                    mediaClient.startRecordAddNotify(cameraIndex, HikSdk.STREAM_TYPE_MAIN, null);
                } else {
                    if(mOnRecordListener!=null){
                        mOnRecordListener.onstopRecord();
                    }
                    mediaClient.stopRecordDelNotify(cameraIndex);
                }
            } else {
                Log.e(TAG, "mediaClient is null");
            }
        }
    }
    public interface OnRecordListener{
        void onstartRecord();
        void onstopRecord();
    }

    @Override
    public void onResultCallback(int i, String s) {

    }
}
