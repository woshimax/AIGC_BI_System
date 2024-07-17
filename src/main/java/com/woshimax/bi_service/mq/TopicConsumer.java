package com.woshimax.bi_service.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, "topic");
        //创建队列（一个消费者一个队列）——queueDeclare
        String queueName1 = "frontend_queue";
        channel1.queueDeclare(queueName1, true, false, false, null);
        //绑定——将交换机和队列关联——queueBind
        channel1.queueBind(queueName1, EXCHANGE_NAME, "#.前端.#");
        String queueName2 = "backend_queue";
        channel1.queueDeclare(queueName2, true, false, false, null);
        channel1.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");
        String queueName3 = "product_queue";
        channel1.queueDeclare(queueName2, true, false, false, null);
        channel1.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
        channel1.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
        channel1.basicConsume(queueName3, true, deliverCallback3, consumerTag -> {
        });
    }
}
