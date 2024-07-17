package com.woshimax.bi_service.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;

@SpringBootTest
class QianfanAiApiTest {

    @Resource
    private QianfanAiApi qianfanAiApi;
    @Test
    void doChat() throws JSONException, IOException {
        qianfanAiApi.doChat("hello");
    }
}