package com.hikdsj.hikdsj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.hikdsj.hikdsj.MainActivity;
import com.hikdsj.hikdsj.MediaActivity;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    public BootReceiver() {
        super();
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(TAG, "自启动了 ！！！！！");
            start(context);
            /*
            Intent newIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            //1.如果自启动APP，参数为需要自动启动的应用包名
            //Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            //这句话必须加上才能开机自动运行app的界面
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //2.如果自启动Activity
            context.startActivity(newIntent);
            //3.如果自启动服务
            //context.startService(newIntent);
            */
        }
    }


    private void start(Context context) {
        Intent start = new Intent(context, StartService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(start);
        } else {
            context.startService(start);
        }
    }


}
