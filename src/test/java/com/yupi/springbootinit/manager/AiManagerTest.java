package com.yupi.springbootinit.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
//使用这个注解是为了让测试类能加载spring的bean
@SpringBootTest
class AiManagerTest {


    @Resource
    private AiManager aiManager;
    @Test
    void doChat() {
        String ans = aiManager.doChat(1651468516836098050L,"邓紫棋");
        System.out.println("answer:"+ans);
    }
}