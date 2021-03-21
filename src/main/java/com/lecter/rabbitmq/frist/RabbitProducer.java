package com.mytest.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;



import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitProducer {
    // 交换器名称
    private static final String EXCHANCE_NAME="exchange_demo";
    // 路由键名称
    private static final String ROUTING_KEY="routingkey_demo";
    // 队列名称
    private static final String QUEUE_NAME="queue_demo";
    // ip地址
    private static final String IP_ADDRESS="121.4.111.40";
    // 端口号
    private static final int PORT = 5672;
//    private static final Logger LOGGER = LoggerFxactory.getLogger(RabbitProducer.class);
    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory =new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root123");
        // 创建连接
        Connection connection = factory.newConnection();

        // 创建信道
        Channel channel = connection.createChannel();
        // 创建type="direct"，持久化的，非自动删除的交换器
        channel.exchangeDeclare(EXCHANCE_NAME,"direct",true,false,null);
        // 创建一个持久化，非排他的，非自动删除的队列
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        // 路由和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANCE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello world";
        channel.basicPublish(EXCHANCE_NAME,ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        channel.close();
        connection.close();
    }

}
