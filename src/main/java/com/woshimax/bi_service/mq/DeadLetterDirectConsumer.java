package com.woshimax.bi_service.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DeadLetterDirectConsumer{

    private static final String EXCHANGE_NAME = "before-dl-direct-exchange";
    private static final String DEAD_EXCHANGE_NAME = "dl-direct-exchange";


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        channel1.exchangeDeclare(EXCHANGE_NAME, "direct");

        //配置args——作为正常队列的参数传入，相当于设置正常任务的死信处理（死信交换机和死信队列）
        Map<String, Object> args = new HashMap<>();
        //指定绑定到死信交换机
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //指定死信丢到哪个死信队列
        args.put("x-dead-letter-routing-key", "od");
        //创建队列（一个消费者一个队列）——queueDeclare
        String queueName1 = "direct21";
        channel1.queueDeclare(queueName1, true, false, false,args);
        //绑定——将交换机和队列关联——queueBind
        channel1.queueBind(queueName1, EXCHANGE_NAME, "direct211");


        Map<String, Object> args1 = new HashMap<>();
        args1.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args1.put("x-dead-letter-routing-key", "boss");
        String queueName2 = "direct22";
        channel1.queueDeclare(queueName2, true, false, false, args1);
        channel1.queueBind(queueName2, EXCHANGE_NAME, "direct222");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //channel1.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);

            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            //channel1.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel1.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel1.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });
    }
}
