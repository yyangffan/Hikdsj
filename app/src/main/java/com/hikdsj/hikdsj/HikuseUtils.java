package com.hikdsj.hikdsj;

import android.content.Context;
import android.util.Log;

import com.hikdsj.hikdsj.base.Constant;
import com.hikvision.hiksdk.HikSdk;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HikuseUtils implements HikSdk.MediaResultCallback {
    private static final String TAG = "HikuseUtils";
    private static volatile HikuseUtils instance;
    private ExecutorService executorService;
    private HikSdk hikSdk;
    private HikSdk.MediaClient mediaClient;
    public static final String video_path = "/storage/sdcard0/carvideo.mp4";
    private int cameraIndex = 0;
    private OnRecordListener mOnRecordListener;
    private boolean isRecoding = false;//是否正在录制
    /*暂时不用   用到再改吧（针对于已开始录制还未结束时又进行了开始的操作）*/
    private String oldRecodNum = "";//上一录制的流水号
    private String recodePath  = "";//上一录制的存储位置


    private HikuseUtils(Context context) {
        executorService = Executors.newCachedThreadPool();
        hikSdk = HikSdk.getInstance(context.getContentResolver());
        mediaClient = hikSdk.getMediaInterface();
        mediaClient.registerMediaResultCallback(this);

    }

    public static HikuseUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (HikuseUtils.class) {
                if (instance == null) {
                    instance = new HikuseUtils(context);
                }
            }
        }
        return instance;
    }

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        mOnRecordListener = onRecordListener;
    }

    /**
     *
     * @param recordNum 流水号
     * @param checkNum  查验次数
     * @param videoType 执法记录仪类型
     * @param start     true-开始  false-结束
     *                  流水号_查验次数_执法记录类型_时间戳.mp4
     */
    public void stspRecod(String recordNum,String checkNum,String videoType, boolean start) {
        if (start /*&& !isRecoding*/) {//如果是开始且未在录制中
            isRecoding = true;
            mediaClient.HikTTSSpeak("流水号"+recordNum+"开始录像");
            String time = String.valueOf(new Date().getTime());
            recordNum += "_" + checkNum +"_" +videoType +"_" +time+".mp4";
            mediaClient.startRecord(0, 0, Constant.VIDEO_FILEP + "start" + recordNum);
        } else {
            isRecoding = false;
            mediaClient.HikTTSSpeak("流水号"+recordNum + "结束录像");
            mediaClient.stopRecord(0);
//            if (start) {
//                isRecoding = true;
////                mediaClient.HikTTSSpeak(recordNum + "开始录制");
//                mediaClient.startRecord(0, 0, "");
//            }

        }
    }

    public interface OnRecordListener {
        void onstartRecord(String path);

        void onstopRecord(String path);
    }

    @Override
    public void onResultCallback(int i, String path) {
        if(isRecoding){
//            recodePath = path;
            mOnRecordListener.onstartRecord(path);
        }else{
            mOnRecordListener.onstopRecord(path);
        }
    }
}
