package com.hikdsj.hikdsj.receiver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hikdsj.hikdsj.HikuseUtils;
import com.hikdsj.hikdsj.MainActivity;
import com.hikdsj.hikdsj.MediaActivity;
import com.hikdsj.hikdsj.R;
import com.hikdsj.hikdsj.base.CarApplication;
import com.hikdsj.hikdsj.base.Constant;
import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;
import com.ljy.devring.util.FileUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StartService extends Service {
    private static final String TAG = "StartService";
    private HikuseUtils mInstance;
    private StringBuilder str_caozuo = new StringBuilder();

    public StartService() {
      initHik();
//        refreshTrueTime();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @TargetApi(26)
    private void setForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        Intent nfIntent = new Intent(this, MediaActivity.class);
        Notification notification = new Notification.Builder(this, getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("请保持程序在后台运行")
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间
                .build();
        startForeground(0x111, notification);
        toConnectSocket();
//        start(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForground() {
        if (Build.VERSION.SDK_INT >= 26) {
            setForeground();
        } else {
            Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
            Intent nfIntent = new Intent(this, MediaActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                    .setContentTitle(getString(R.string.app_name)) // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("请保持程序在后台运行") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
            Notification notification = builder.build(); // 获取构建好的Notification
            startForeground(0x111, notification);// 开始前台服务
            toConnectSocket();
//            start(this);
        }
    }

    private void goUpVideo() {
        Log.i(TAG, "goUpVideo: 录制结束，上传视频");
        str_caozuo.append("\n:录制结束，上传视频" + "/storage/sdcard0/carvideo.mp4");
        toSaveLog(str_caozuo.toString(), "结束：");
    }

    private void toSaveLog(String result, String msg) {
        String out_str = msg + ": " + result;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            String time = simpleDateFormat.format(new Date());
            String fileName = time + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirTemp = FileUtil.getDirectory(FileUtil.getExternalCacheDir(this), "carcontrol_log");
                File fileOutput = FileUtil.getFile(dirTemp, fileName);

                if (fileOutput == null) {
                    Log.e(TAG, "toSaveLog: 文件创建失败");
                    return;
                }
                FileOutputStream fos = new FileOutputStream(fileOutput);
                fos.write(out_str.getBytes());
                fos.close();
                Log.e(TAG, "toSaveLog: 文件已保存");
            }
        } catch (Exception e) {
            Log.e(TAG, "toSaveLog: 文件创建失败" + e.toString());
        }
        str_caozuo = new StringBuilder();
    }
    /*海康SDK工具类初始化*/
    private void initHik() {
        mInstance = HikuseUtils.getInstance(CarApplication.getInstance());
        mInstance.setOnRecordListener(new HikuseUtils.OnRecordListener() {
            @Override
            public void onstartRecord() {
            }

            @Override
            public void onstopRecord() {
                goUpVideo();
            }
        });
    }

    /*--------------------------------------时间校准--------------------------------------*/
    private void refreshTrueTime() {
        if (!TrueTimeRx.isInitialized()) {
            Log.e(TAG, "refreshTrueTime: Sorry TrueTime not yet initialized.");
            return;
        }
        Date trueTime = TrueTimeRx.now();
        Date deviceTime = new Date();

        Log.d(TAG, String.format(" [trueTime: %d] [devicetime: %d] [drift_sec: %f]", trueTime.getTime(), deviceTime.getTime(), (trueTime.getTime() - deviceTime.getTime()) / 1000F));
        Log.e(TAG, "_formatDate: "+ _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss"));
        Log.e(TAG, "refreshTrueTime: " );
//        SystemClock.setCurrentTimeMillis(trueTime.getTime());
    }

    private String _formatDate(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    /*--------------------------------------Socket一系列--------------------------------------*/
    private String mContect_ip = "";                        //设备间的唯一标识
    private WebSocketClient mWebSocketClient;
    private static final long HEART_BEAT_RATE = 30 * 1000;  //心跳间隔
    private long sendTime = 0L;                             //心跳时间暂存
    private String mSocket_url = "";

    private void toConnectSocket() {
        mContect_ip = "10";
        initSocket();

    }

    public void initSocket() {
        if (null == mWebSocketClient) {
            mSocket_url = Constant.BASE_URL + "webSocket/" + mContect_ip;
            Log.e(TAG, "initSocket: " + mSocket_url);
            try {
                mWebSocketClient = new WebSocketClient(new URI(mSocket_url)) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.i(TAG, "State_Socket：连接成功");
                    }

                    @Override
                    public void onMessage(String message) {
                        str_caozuo.append("onMessage:" + message);
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            String type = jsonObject.getString("type");
                            if (type.equals("1")) {
                                mInstance.stspRecod(true);
                                str_caozuo.append("\n:开始录制");
                            } else if (type.equals("2")) {
                                str_caozuo.append("\n:结束录制");
                                mInstance.stspRecod(false);
                            }
                        } catch (JSONException e) {
                            str_caozuo.append("\n:解析异常" + e.toString());
                        }

                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.e(TAG, "State_Socket：已关闭- code:" + code + " reason:" + reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.e(TAG, "State_Socket：连接错误-" + ex.toString());
                    }
                };
                mWebSocketClient.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
                toStartHeart();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                toStartHeart();
            }
            toStartHeart();
        }
    }


    private void toStartHeart() {
        Log.i(TAG, "initSocket: 启动心跳");
        mHandler_socket.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
    }

    private Handler mHandler_socket = new Handler();
    Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                if (mWebSocketClient != null) {//长连接已断开
                    if (mWebSocketClient.isClosed()) {
                        reconnectWs();
                    }
                    Log.e(TAG, "run: 发送心跳");
                } else {//长连接处于连接状态
                    initSocket();
                }
                sendTime = System.currentTimeMillis();

            }
            mHandler_socket.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mWebSocketClient.reconnectBlocking();
                    Log.e(TAG, "State_Socket：重新连接中..." + mSocket_url);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void stopConnect() {
        if (mWebSocketClient != null) {
            try {
                mWebSocketClient.close();
                mWebSocketClient = null;
                Log.e(TAG, "stopConnect: 链接已断开 " + mSocket_url);
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.toString());
            }
        }
        mHandler_socket.removeCallbacks(heartBeatRunnable);
    }
    /*sendBroadcast(new Intent(this,MyLiveReceiver.class));*/
  /*  private void start(Context context) {
        Intent int_media = new Intent(context, MediaActivity.class);
        int_media.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(int_media);
    }*/

}