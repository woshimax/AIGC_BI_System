package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RedisLimiterManagerTest {


    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimit() {
        for (int i = 0; i < 10; i++) {
            redisLimiterManager.doRateLimit("me");
            System.out.println("第"+i+"次成功");
        }
    }
}