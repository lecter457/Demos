package com.lecter.rabbitmq.second;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Endurance {
    // 交换器名称
    private static final String EXCHANGE_NAME = "exchange_name";
    // 路由键名
    private static final String ROUTING_KEY = "routingkey_name";
    // 队列名
    private static final String QUEUE_NAME = "queue_name";
    // ip地址
    private static final String IP_ADDRESS = "127.0.0.1";
    // 端口
    private static final int PORT = 5672;

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory =new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root123");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建信道
        Channel channel =connection.createChannel();
        // 创建持久化，非排他的,非自动删除的交换器
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,null);
        // 创建持久化、非排他的、非自动删除的队列
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        // 将交换器和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello World";
        // 将当前信道设置成事务模式
        channel.txSelect();
        // 消息发布模式修改
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,MessageProperties.PERSISTENT_TEXT_PLAIN,"持久化消息".getBytes());
        channel.txCommit();
//        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY, MessageProperties.MINIMAL_PERSISTENT_BASIC,message.getBytes());
        channel.close();
        connection.close();
    }
}
