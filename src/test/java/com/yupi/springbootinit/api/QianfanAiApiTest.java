package com.yupi.springbootinit.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QianfanAiApiTest {

    @Resource
    private QianfanAiApi qianfanAiApi;
    @Test
    void doChat() throws JSONException, IOException {
        qianfanAiApi.doChat("hello");
    }
}