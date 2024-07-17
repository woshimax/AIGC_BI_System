package com.woshimax.bi_service.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class DeadLetterDirectProducer {
    private static final String DEAD_EXCHANGE_NAME = "dl-direct-exchange";
    private static final String EXCHANGE_NAME = "before-dl-direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //创建死信队列交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");
            //创建死信队列
            String queueName1 = "boss_dl_queue";
            channel.queueDeclare(queueName1,true,false,false,null);
            channel.queueBind(queueName1,DEAD_EXCHANGE_NAME,"boss");
            String queueName2 = "od_dl_queue";
            channel.queueDeclare(queueName2,true,false,false,null);
            channel.queueBind(queueName2,DEAD_EXCHANGE_NAME,"od");

            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String input = scanner.nextLine();
                String[] split = input.split(" ");
                if(split.length < 1) continue;
                String message = split[0];
                String routingKey = split[1];
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

            }


        }
    }
}
