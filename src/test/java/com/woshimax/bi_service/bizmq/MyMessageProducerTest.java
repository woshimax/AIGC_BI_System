package com.woshimax.bi_service.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MyMessageProducerTest {

    @Resource
    private MyMessageProducer myMessageProducer;
    @Test
    void sentMessage() {
        myMessageProducer.sentMessage("code-exchange","my_routingKey","hello world");
    }
}