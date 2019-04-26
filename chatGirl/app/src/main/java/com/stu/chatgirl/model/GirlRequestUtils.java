package com.stu.chatgirl.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GirlRequestUtils {

    private String strResponse;

    private OkHttpClient client;

    public String sendGet(String url) {
        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Exception", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.cacheResponse() != null) {
                    strResponse = response.cacheResponse().toString();
                    Log.i("result", strResponse);
                }
            }
        });
        return strResponse;
    }

    /***
     * post请求数据
     * @param url 请求地址
     * @param keys 请求参数的key值数组
     * @param values 请求参数的key对应的value数组
     * @return 返回请求结果
     * @throws IOException
     */
    public String sendPost(String content) throws IOException {
        client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", "e30fa1c4d3684b94aea9c12bee6f8214");
            jsonObject.put("info", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder().post(requestBody).url("http://www.tuling123.com/openapi/api").build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException();
        }
        strResponse = response.body().string();
        return strResponse;
    }
}
