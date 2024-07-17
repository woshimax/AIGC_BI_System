package com.woshimax.bi_service.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyMessageProducer {
    //类似springboot的redis，使用redisTemplate，简单易用
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sentMessage(String exchange,String routingKey,String msg){
        rabbitTemplate.convertAndSend(exchange,routingKey,msg);
    }
}

