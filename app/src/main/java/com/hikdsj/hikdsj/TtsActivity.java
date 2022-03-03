package com.hikdsj.hikdsj;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.hikvision.hiksdk.HikSdk;

public class TtsActivity extends AppCompatActivity
{
    private HikSdk hikSdk;
    private HikSdk.MediaClient ttsClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tts_layout);
        hikSdk = HikSdk.getInstance(getContentResolver());
        ttsClient = hikSdk.getMediaInterface();;
        findViewById(R.id.start_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speakStr = getString(R.string.tts_speak);
                new Thread(new TTsThread(speakStr,speakStr.length())).start();
            }
        });
        findViewById(R.id.stop_tts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.HikTTSStop();
            }
        });
    }

    class TTsThread implements Runnable
    {
        String context;
        int len;
        TTsThread(String context, int len)
        {
            this.context = context;
            this.len = len;
        }
        @Override
        public void run()
        {
            ttsClient.HikTTSSpeak(context);
        }
    }
}
