package com.hikdsj.hikdsj;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hikvision.hiksdk.HikSdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MediaActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        HikSdk.MediaResultCallback, View.OnClickListener {

    private static final String TAG = "HikSdkDemo";

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Button pic, startRecode, endRecode;

    private ExecutorService executorService;

    private HikSdk hikSdk;
    private HikSdk.MediaClient mediaClient;
    private HikSdk.Parameter parameter;

    private final Object cameraLock = new Object();
    private int cameraIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        initData();
        initView();
    }

    private void initData() {
        executorService = Executors.newCachedThreadPool();
        hikSdk = HikSdk.getInstance(getContentResolver());
        mediaClient = hikSdk.getMediaInterface();
        parameter = hikSdk.getParameterInterface();
        mediaClient.registerMediaResultCallback(this);
    }

    private void initView() {
        surfaceView = findViewById(R.id.sv);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        pic = findViewById(R.id.pic);
        startRecode = findViewById(R.id.startrecode);
        endRecode = findViewById(R.id.stoprecode);
        pic.setOnClickListener(this);
        startRecode.setOnClickListener(this);
        endRecode.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        mediaClient.unregisterMediaResultCallback();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
        executorService.submit(new StartPreviewRunnable());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        executorService.submit(new StopPreviewRunnable());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pic:
                executorService.submit(new TakePicRunnable());
                mediaClient.HikTTSSpeak("test");
                Log.d(TAG, "########" + mediaClient.queryAudioStatus());
                break;
            case R.id.startrecode:
//                executorService.submit(new RecordRunnable());
                mediaClient.startRecord(0, 0, "");
                break;
                case R.id.stoprecode:
//                executorService.submit(new RecordRunnableStop());
                mediaClient.stopRecord(0);
                break;


        }
    }

    @Override
    public void onResultCallback(int i, final String s) {
        //i = 1001?????????????????????
        //i = 1006????????????
        //i = 1007????????????
        Log.e(TAG, "i:" + i + "   s:" + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(com.hikdsj.hikdsj.MediaActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class StartPreviewRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "StartPreviewRunnable");
            synchronized (cameraLock) {
                if (mediaClient != null) {
                    Log.e(TAG, "StartPreviewRunnable--");
                    long start = System.currentTimeMillis();
                    String Resolution_0_0 = parameter.getStrPara(HikSdk.Parameter.RecStreamChan0.RESOLUTION);
                    Log.e(TAG, "Resolution_0_0???" + Resolution_0_0);
                    mediaClient.setPreviewSurface(cameraIndex, holder.getSurface());
                    mediaClient.startPreview(cameraIndex);
                    //mediaClient.startGetPreviewData(cameraIndex, 5, mediaResultCallback);
                } else {
                    Log.e(TAG, "mediaClient is null");
                }
            }
            //?????????????????????
            mediaClient.startGetPreviewData(0, 0xCC);
            Log.e(TAG, "startGetPreviewData");
        }
    }

    private class StopPreviewRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "StopPreviewRunnable");
            synchronized (cameraLock) {
                if (mediaClient != null) {
                    Log.e(TAG, "StopPreviewRunnable--");
                    long start = System.currentTimeMillis();
                    mediaClient.stopPreview(cameraIndex);
                } else {
                    Log.e(TAG, "mediaClient is null");
                }
            }
            //?????????????????????
            mediaClient.stopGetPreviewData(0);
            Log.e(TAG, "stopGetPreviewData");
        }
    }

    private class TakePicRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "TakePicRunnable");
            if (mediaClient != null) {
                /**
                 * takePicture
                 * @param channel; channel number.
                 * @param mode; default 1, save the file in the SD card
                 * @param path; If you do not set a path, please set the path  is "" or null(default path).
                 * @return -1 fail; Otherwise success
                 * @throws RemoteException
                 */
                mediaClient.takePicture(cameraIndex, 1, 1, "/sdcard/111.jpg");
            } else {
                Log.e(TAG, "mediaClient is null");
            }
        }
    }

    private class RecordRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "RecordRunnable");
            if (mediaClient != null) {
                //1?????? ???????????????
                mediaClient.setFileTags(1, "JQ12344212412", null);
                int ret = mediaClient.queryRecordStatus(cameraIndex);
                mediaClient.startRecordAddNotify(cameraIndex, HikSdk.STREAM_TYPE_MAIN, "/storage/sdcard0/carvideo.mp4");
            } else {
                Log.e(TAG, "mediaClient is null");
            }
        }
    }

    private class RecordRunnableStop implements Runnable {

        @Override
        public void run() {
            Log.e(TAG, "RecordRunnable");
            if (mediaClient != null) {
                //1?????? ???????????????
                mediaClient.setFileTags(1, "JQ12344212412", null);
                mediaClient.stopRecordDelNotify(cameraIndex);
            } else {
                Log.e(TAG, "mediaClient is null");
            }
        }
    }
}
