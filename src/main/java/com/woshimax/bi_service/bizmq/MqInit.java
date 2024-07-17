package com.woshimax.bi_service.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 注意：队列提前使用main函数创建好，程序运行时使用队列而不创建队列
 */
public class MqInit {
    public static void main(String[] args) {
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(BiConstant.EXCHANGE_NAME, "direct");

            //创建队列（一个消费者一个队列）——queueDeclare

            channel.queueDeclare(BiConstant.QUEUE_NAME,true,false,false,null);
            //绑定——将交换机和队列关联——queueBind
            channel.queueBind(BiConstant.QUEUE_NAME,BiConstant.EXCHANGE_NAME,BiConstant.ROUTING_KEY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
