package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class FanoutConsumer {
    //交换机之广播——发布订阅模式，老板把消息分发给每个员工（广播）
    private static final String EXCHANGE_NAME = "fanout-exchange";//和producer声明的交换机一样

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();

        //同样的，和producer声明的一样
        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");


        //创建队列，随机分配队列名称（getQueue）
        //String queueName = channel.queueDeclare().getQueue();

        //创建队列（一个消费者一个队列）——queueDeclare
        String queueName1 = "小王";
        channel1.queueDeclare(queueName1,true,false,false,null);
        //绑定——将交换机和队列关联——queueBind
        channel1.queueBind(queueName1,EXCHANGE_NAME,"");
        String queueName2 = "小李";
        channel1.queueDeclare(queueName2,true,false,false,null);
        channel1.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小王] Received '" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小李] Received '" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
        channel1.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }
}
