package com.lecter.rabbitmq.second;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeadLetterExchange {
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

    public static void main(String[] args) throws IOException, TimeoutException {
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
        // 创建死信队列的交换器
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");
        // 设置死信参数
        Map<String,Object> dlxArgs = new HashMap<String, Object>();
        dlxArgs.put("x-dead-letter-exchange","dlx_exchange");
        // 指定死信队列路由键
        dlxArgs.put("x-dead-letter-routing-key","dlx-routing-key");
        // policy方式指定
        // rabbitmqctl set_policy DLX ".*" '{"dead-letter-exchange":" dlx_exchange "}' --apply-to queues
        // 创建死信队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,dlxArgs);
        // 将交换器和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello World";
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY, MessageProperties.MINIMAL_PERSISTENT_BASIC,message.getBytes());
        channel.close();
        connection.close();
    }
}
