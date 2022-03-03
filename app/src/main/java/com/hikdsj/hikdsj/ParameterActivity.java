package com.hikdsj.hikdsj;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hikvision.hiksdk.HikSdk;

public class ParameterActivity extends AppCompatActivity {

    private static final String TAG = "ParameterActivity";

    private HikSdk hikSdk;
    private HikSdk.Parameter parameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);
        initData();
        String deviceName = parameter.getStrPara(HikSdk.Parameter.NETSDK_DEVICE_NAME);
        Log.e(TAG, deviceName);
        Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
    }

    private void initData() {
        hikSdk = HikSdk.getInstance(getContentResolver());
        parameter = hikSdk.getParameterInterface();
    }
}
