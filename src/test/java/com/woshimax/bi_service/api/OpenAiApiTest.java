package com.woshimax.bi_service.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class OpenAiApiTest {

    @Resource
    private OpenAiApi openAiApi;
    @Test
    void doChat() {
        openAiApi.doChat();
    }
}