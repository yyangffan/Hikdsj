package com.hikdsj.hikdsj;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.hikdsj.hikdsj.receiver.StartService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start(this);
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
}
