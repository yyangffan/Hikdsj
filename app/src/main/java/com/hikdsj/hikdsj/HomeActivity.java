package com.hikdsj.hikdsj;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hikdsj.hikdsj.base.CarApplication;
import com.hikdsj.hikdsj.base.Constant;
import com.hikdsj.hikdsj.bean.EventMessage;
import com.hikdsj.hikdsj.receiver.StartService;
import com.hikdsj.hikdsj.utils.CarShareUtil;
import com.hikdsj.hikdsj.utils.UpFileUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {
    EditText mConfigSetIp;
    EditText mConfigSetKou;
    EditText mConfigSetName;
    private boolean can_save = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        startService();
    }

    private void initViews() {
        findViewById(R.id.textView6).requestFocus();
        mConfigSetIp = findViewById(R.id.config_set_ip);
        mConfigSetKou = findViewById(R.id.config_set_kou);
        mConfigSetName = findViewById(R.id.config_set_name);
        String fwq_ip = (String)CarShareUtil.getInstance().get(CarShareUtil.FWQ_IP, "172.16.98.96");
        String fwq_dkh = (String)CarShareUtil.getInstance().get(CarShareUtil.FWQ_DKH, "8912");
        String fwq_inter = (String)CarShareUtil.getInstance().get(CarShareUtil.FWQ_INT, "interface");
        mConfigSetIp.setText(fwq_ip);
        mConfigSetKou.setText(fwq_dkh);
        mConfigSetName.setText(fwq_inter);

    }

    /*????????????-3???????????????*/
    public void goTest(View v) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.readTimeout(3, TimeUnit.SECONDS).connectTimeout(3, TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getUrl())
                .client(client)
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<JSONObject> call = service.connectionTest();
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> bean) {
                //??????????????????
                Toast.makeText(HomeActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                can_save = true;
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                //??????????????????
                Toast.makeText(HomeActivity.this, "????????????,???????????????", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void toSetUrl(View view) {
        if (can_save) {
            toSetBaseUrl();
            CarShareUtil.getInstance().put(CarShareUtil.APP_BASEURL, Constant.BASE_URL);
            EventBus.getDefault().post(new EventMessage("diss"));
            Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "??????????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /*?????????????????????*/
    private void toSetBaseUrl() {
        Constant.BASE_URL = getUrl();
        CarApplication.getInstance().init();
        CarShareUtil.getInstance().put(CarShareUtil.FWQ_IP, mConfigSetIp.getText().toString());
        CarShareUtil.getInstance().put(CarShareUtil.FWQ_DKH, mConfigSetKou.getText().toString());
        CarShareUtil.getInstance().put(CarShareUtil.FWQ_INT, mConfigSetName.getText().toString());
    }

    /*???????????????????????????*/
    private String getUrl() {
        String url = "http://";
        url += mConfigSetIp.getText().toString();
        url += ":" + mConfigSetKou.getText().toString();
        url += "/" + mConfigSetName.getText().toString() + "/";
        return url;
    }

    /*????????????*/
    private void startService() {
        rxPermission();
        Intent start = new Intent(this, StartService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(start);
        } else {
            startService(start);
        }
    }

    /*??????????????????--????????????????????????????????????*/
    private void rxPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                if (!granted) {
                    Toast.makeText(HomeActivity.this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}