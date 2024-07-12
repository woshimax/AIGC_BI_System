package com.yupi.springbootinit.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OpenAiApiTest {

    @Resource
    private OpenAiApi openAiApi;
    @Test
    void doChat() {
        openAiApi.doChat();
    }
}