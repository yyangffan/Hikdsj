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
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hikdsj.hikdsj.ApiService;
import com.hikdsj.hikdsj.DeviceUtil;
import com.hikdsj.hikdsj.HikuseUtils;
import com.hikdsj.hikdsj.HomeActivity;
import com.hikdsj.hikdsj.MainActivity;
import com.hikdsj.hikdsj.MediaActivity;
import com.hikdsj.hikdsj.R;
import com.hikdsj.hikdsj.base.CarApplication;
import com.hikdsj.hikdsj.base.Constant;
import com.hikdsj.hikdsj.bean.EventMessage;
import com.hikdsj.hikdsj.bean.VideoBean;
import com.hikdsj.hikdsj.utils.UpFileUtils;
import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.throwable.HttpThrowable;
import com.ljy.devring.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StartService extends Service {
    private static final String TAG = Constant.TAG;
    private UpFileUtils mUpFileUtils;
    private HikuseUtils mInstance;
    private String device_ip = "10";                          //????????????????????????
    private WebSocketClient mWebSocketClient;
    private static final long HEART_BEAT_RATE = 30 * 1000;  //????????????
    private long sendTime = 0L;                             //??????????????????
    private String mSocket_url = "";
    public static final long UPFILE_TIME = 10 * 1000;       //???????????????????????????
    private long upFileTime = 0L;                           //???????????????????????????
    private boolean upFileing = false;                      //??????????????????
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Toast.makeText(StartService.this, "????????????,?????????????????????", Toast.LENGTH_SHORT).show();
        }
    };

    public StartService() {
        init();
    }

    private void init() {
        device_ip = DeviceUtil.getIpAddressString();
        mUpFileUtils = UpFileUtils.getInstance();
        startTaskTimer();
        initHik();
        mUpFileUtils.setOnUpFileListener(new UpFileUtils.OnUpFileListener() {
            @Override
            public void onStartUpListener() {
                upFileing = true;
            }

            @Override
            public void onFinishListener() {
                upFileing = false;
            }
        });
    }

    /*??????SDK??????????????????*/
    private void initHik() {
        mInstance = HikuseUtils.getInstance(CarApplication.getInstance());
        mInstance.setOnRecordListener(new HikuseUtils.OnRecordListener() {
            @Override
            public void onstartRecord(String path) {
                Log.e(TAG, "??????????????? path= " + path);
            }

            @Override
            public void onstopRecord(String path) {
                File file = FileUtil.getFile(path);
                String name = file.getName();
                name = name.replaceAll("start", "");
                file.renameTo(new File(Constant.VIDEO_FILEP, name));
                Log.e(TAG, "onstopRecord: " + name);
//                goUpVideo(path);
            }
        });
    }

    /*??????????????????*/
    private void goUpVideo(String path) {
        Log.e(TAG, "???????????? path= " + path);
        final File video = new File(path);
        String names = video.getName();
        RequestBody requestFile = RequestBody.create(MediaType.parse(guessMimeType(video.getPath())), video);
        MultipartBody.Part body = null;
        try {
            body = MultipartBody.Part.createFormData("upvideoFile", URLEncoder.encode(names, "UTF-8"), requestFile);
        } catch (UnsupportedEncodingException e) {
            Log.e("ManOneFragment", "toAddClient: ???????????????" + names + e.toString());
        }
        DevRing.httpManager().commonRequest(DevRing.httpManager().getService(ApiService.class).upLoadFile(body), new CommonObserver<com.alibaba.fastjson.JSONObject>() {
            @Override
            public void onResult(com.alibaba.fastjson.JSONObject result) {
                VideoBean bean = new Gson().fromJson(result.toString(), VideoBean.class);
                if (bean.getCode() == 200) {
                    FileUtil.deleteFile(video, true);
                }
                Log.d(TAG, result.toJSONString());
            }

            @Override
            public void onError(HttpThrowable throwable) {
                Log.e(TAG, "onError: " + throwable.toString());
            }
        }, TAG);
    }

    /*--------------------------------------Socket?????????--------------------------------------*/
    /*????????????-3???????????????*/
    public void goTest() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.readTimeout(3, TimeUnit.SECONDS).connectTimeout(3, TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constant.BASE_URL)
                .client(client)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<com.alibaba.fastjson.JSONObject> call = service.connectionTest();
        call.enqueue(new Callback<com.alibaba.fastjson.JSONObject>() {
            @Override
            public void onResponse(Call<com.alibaba.fastjson.JSONObject> call, Response<com.alibaba.fastjson.JSONObject> bean) {
                Log.i(TAG, "onResponse: ????????????--????????????Socket" + Constant.BASE_URL);
                initSocket();
            }

            @Override
            public void onFailure(Call<com.alibaba.fastjson.JSONObject> call, Throwable t) {
                Log.i(TAG, "onResponse: ????????????--????????????Socket");
                mHandler.sendEmptyMessage(1);
                mHandler_socket.removeCallbacks(heartBeatRunnable);
            }
        });
    }

    private void toConnectSocket() {
        goTest();
    }

    public void initSocket() {
        if (null == mWebSocketClient) {
            mSocket_url = Constant.BASE_URL + "webSocket/" + device_ip;
            Log.e(TAG, "initSocket: " + mSocket_url);
            try {
                mWebSocketClient = new WebSocketClient(new URI(mSocket_url)) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.i(TAG, "State_Socket???????????????");
                    }

                    @Override
                    public void onMessage(String message) {
                        Log.e(TAG, "onMessage: " + message);
                        /*"type"?????? 1?????? 2????????????  "checkNum"????????????  "videoType"?????????????????????  "lsh": ?????????*/
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            String type = jsonObject.getString("type");
                            String liushuiCode = jsonObject.getString("lsh");
                            if (type.equals("1")) {
                                String checkNum = jsonObject.getString("checkNum");
                                String videoType = jsonObject.getString("videoType");
                                mInstance.stspRecod(liushuiCode,checkNum,videoType, true);
                            } else if (type.equals("2")) {
                                mInstance.stspRecod(liushuiCode, "", "", false);
                            } else if (type.equals("3")) {
//                                UpFileUtils.getInstance().toCheckFile();
                            }
                        } catch (JSONException e) {
                        }

                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.e(TAG, "State_Socket????????????- code:" + code + " reason:" + reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.e(TAG, "State_Socket???????????????-" + ex.toString());
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
        Log.i(TAG, "initSocket: ????????????");
        mHandler_socket.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//??????????????????
    }

    private Handler mHandler_socket = new Handler();
    Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                if (mWebSocketClient != null) {//??????????????????
                    if (mWebSocketClient.isClosed()) {
                        reconnectWs();
                    }
                    Log.e(TAG, "run: ????????????");
                } else {//???????????????????????????
                    initSocket();
                }
                sendTime = System.currentTimeMillis();
            }
            mHandler_socket.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        }
    };

    private BroadcastTimerTaskTask mTask_broas;
    private Timer mBroad_task;// ????????? Timer

    /**
     * ?????????
     */
    private class BroadcastTimerTaskTask extends TimerTask {
        public void run() {
            if (System.currentTimeMillis() - upFileTime >= UPFILE_TIME && !upFileing) {
                mUpFileUtils.toCheckFile();
                upFileTime = System.currentTimeMillis();
            }
        }
    }

    private void startTaskTimer() {
        upFileTime = 0l;
        if (mBroad_task == null) {
            mBroad_task = new Timer(true);
            mTask_broas = new BroadcastTimerTaskTask();
            mBroad_task.schedule(mTask_broas, 1000, 1000);
        }
    }

    /**
     * ????????????
     */
    private void reconnectWs() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mWebSocketClient.reconnectBlocking();
                    Log.e(TAG, "State_Socket??????????????????..." + mSocket_url);
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
                toConnectSocket();
                Log.e(TAG, "stopConnect: ??????????????? " + mSocket_url);
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.toString());
            }
        } else {
            toConnectSocket();
        }
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

    /*sendBroadcast(new Intent(this,MyLiveReceiver.class));*/
  /*  private void start(Context context) {
        Intent int_media = new Intent(context, MediaActivity.class);
        int_media.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(int_media);
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @TargetApi(26)
    private void setForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        Intent nfIntent = new Intent(this, HomeActivity.class);
        Notification notification = new Notification.Builder(this, getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // ??????PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.logo)) // ??????????????????????????????(?????????)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.smlogo)
                .setContentText("??????????????????????????????")
                .setWhen(System.currentTimeMillis()) // ??????????????????????????????
                .build();
        startForeground(0x111, notification);
        toConnectSocket();
//        start(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        startForground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForground() {
        if (Build.VERSION.SDK_INT >= 26) {
            setForeground();
        } else {
            Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //????????????Notification?????????
            Intent nfIntent = new Intent(this, HomeActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // ??????PendingIntent
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.logo)) // ??????????????????????????????(?????????)
                    .setContentTitle(getString(R.string.app_name)) // ??????????????????????????????
                    .setSmallIcon(R.mipmap.smlogo) // ??????????????????????????????
                    .setContentText("??????????????????????????????") // ?????????????????????
                    .setWhen(System.currentTimeMillis()); // ??????????????????????????????
            Notification notification = builder.build(); // ??????????????????Notification
            startForeground(0x111, notification);// ??????????????????
            toConnectSocket();
//            start(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMsgt(EventMessage msg) {
        if (msg.getMessage().equals("diss")) {
            stopConnect();
        }
    }


}