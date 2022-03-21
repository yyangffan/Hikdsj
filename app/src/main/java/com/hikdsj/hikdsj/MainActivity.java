package com.hikdsj.hikdsj;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.functions.Consumer;

import android.os.Bundle;
import android.widget.Toast;

import com.hikdsj.hikdsj.receiver.StartService;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start(this);
        rxPermission();
        findViewById(R.id.btn_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_parameter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ParameterActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TtsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void start(Context context) {
        Intent start = new Intent(context, StartService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(start);
        } else {
            context.startService(start);
        }
    }

    private void rxPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                if (granted) {

                } else {
                    Toast.makeText(MainActivity.this, "没有权限无法上传视频", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
