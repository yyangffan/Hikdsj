package com.hikdsj.hikdsj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyLiveReceiver extends BroadcastReceiver {
    private static final String TAG = "MyLiveReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: 收到消息了");
    }
}
