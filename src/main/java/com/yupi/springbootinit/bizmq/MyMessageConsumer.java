package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")//这个注解会帮我们自动填充msg,channel和delivery对象;指定监听的消息队列
    public void receiveMessage(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag) throws IOException {
        log.info("receive message = {}",msg);
        channel.basicAck(deliveryTag,false);
    }
}
