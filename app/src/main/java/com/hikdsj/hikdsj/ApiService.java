package com.hikdsj.hikdsj;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    /**
     * 视频上传
     *
     * @param map 文件
     * @return
     */
    @Multipart
    @POST("cancellation/uploadVideo")
    Observable<JSONObject> upLoadFile(@Part MultipartBody.Part map);

    /**
     * 连接测试
     *
     * @return
     */
    @POST("cancellation/connectionTest")
    Call<JSONObject> connectionTest();
}
