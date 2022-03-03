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
import android.os.IBinder;

import com.hikdsj.hikdsj.MainActivity;
import com.hikdsj.hikdsj.MediaActivity;
import com.hikdsj.hikdsj.R;

public class StartService extends Service {
    public StartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

/*    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent int_media = new Intent(this, MediaActivity.class);
        int_media.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(int_media);
        return super.onStartCommand(intent, flags, startId);
    }*/

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
//        toConnectSocket();
        start(this);

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
//            toConnectSocket();
            start(this);
        }
    }




    private void start(Context context) {
        Intent int_media = new Intent(context, MediaActivity.class);
        int_media.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent.getActivity(context, 0, int_media, 0);
        startActivity(int_media);
    }





}