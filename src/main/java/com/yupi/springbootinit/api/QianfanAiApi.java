package com.yupi.springbootinit.api;

import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import lombok.Data;
import okhttp3.*;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Data
@ConfigurationProperties(prefix = "qianfanai")
public class QianfanAiApi {
    private String apiKey;
    private String secretKey;
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new RetryInterceptor())
            .build();

    public String doChat(String msg) throws JSONException, IOException {
        HashMap<String, Object> hashMap = new HashMap<>();
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, String>(){{
            put("role", "user");
            put("content", msg);
            put("temperature",String.valueOf(0.95));
            put("top_p",String.valueOf(0.8));
            put("penalty_score",String.valueOf(1));
            put("disable_search","false");
            put("enable_citation","false");
        }});
        hashMap.put("messages", dataList);
        String json = JSONUtil.toJsonStr(hashMap);
        System.out.println(json);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if(response.body() == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
        }
        return response.body().string();
    }

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    public String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + apiKey
                + "&client_secret=" + secretKey);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
    static class RetryInterceptor implements Interceptor {
        private static final int MAX_RETRIES = 3;

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;
            int tryCount = 0;

            while (tryCount < MAX_RETRIES && (response == null || !response.isSuccessful())) {
                try {
                    response = chain.proceed(request);
                } catch (IOException e) {
                    exception = e;
                }
                tryCount++;
            }

            if (response == null && exception != null) {
                throw exception;
            }
            return response;
        }
    }
}
