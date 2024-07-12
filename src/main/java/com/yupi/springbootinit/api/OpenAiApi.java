package com.yupi.springbootinit.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Data
@ConfigurationProperties(prefix = "openai")
public class OpenAiApi {
    private String apiKey;
    public void doChat(){
        String url = "https://api.openai.com/v1/chat/completions";
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("model", "gpt-3.5-turbo");
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, String>(){{
            put("role", "user");
            put("content", "传给ai的信息，帮我分析");
        }});
        hashMap.put("messages", dataList);
        String json = JSONUtil.toJsonStr(hashMap);

        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer "+apiKey)
                .header("Content-Type", "application/json")
                .body(json)
                .execute()
                .body();

        System.out.println(result);
    }

}
