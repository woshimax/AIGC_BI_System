package com.yupi.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class BiProducer {

    //类似springboot的redis，使用redisTemplate，简单易用
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sentMessage(String msg){
        rabbitTemplate.convertAndSend(BiConstant.EXCHANGE_NAME,BiConstant.ROUTING_KEY,msg);
    }
}
