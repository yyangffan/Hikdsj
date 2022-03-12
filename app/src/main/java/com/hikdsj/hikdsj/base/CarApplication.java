package com.hikdsj.hikdsj.base;

import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;
import com.ljy.devring.DevRing;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CarApplication extends Application {
    private static final String TAG = "CarApplication";
    private static volatile CarApplication instance;

    public static CarApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
        initRxTrueTime();
    }

    /*网络框架初始化*/
    private void init() {
        try {
            File cacheDir =new  File(getCacheDir(), "https");
            HttpResponseCache.install(cacheDir, 1024 * 1024 * 128);
        } catch (IOException e) {
            Log.e(TAG, "init: "+e.toString() );
        }
        DevRing.init(this);
        DevRing.configureHttp().setBaseUrl(Constant.BASE_URL).setIsUseCookie(true).setConnectTimeout(60).setIsUseLog(true);
        DevRing.configureOther().setIsUseCrashDiary(true);
        DevRing.configureImage();
        DevRing.create();
        // 获取uimode系统服务---为深色、夜间模式的相关逻辑
        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
//        int currentMode = uiModeManager.getNightMode();
        /*设置夜间状态 UiModeManager.MODE_NIGHT_AUTO ⾃动  UiModeManager.MODE_NIGHT_YES 启⽤*/
        uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO); // 停⽤
    }
    /**
     * Initialize the TrueTime using RxJava.
     * 中科大ftp服务器 "time.ustc.edu.cn"
     */
    private void initRxTrueTime() {
        DisposableSingleObserver<Date> disposable = TrueTimeRx.build()
                .withConnectionTimeout(600)
                .withRetryCount(100)
                .withSharedPreferencesCache(this)
                .withLoggingEnabled(true)
                .initializeRx("http://47.104.224.247:8111/")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Date>() {
                    @Override
                    public void onSuccess(Date date) {
                        Log.e(TAG, "Success initialized TrueTime :" + date.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                    }
                });
    }

}
