package com.lecter.rabbitmq.frist;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitmqProducer {
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
        // 将交换器和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello World";
        /**
         * 消息配置
         *      MINIMAL_BASIC
         *      MINIMAL_PERSISTENT_BASIC
         *      BASIC
         *      PERSISTENT_BASIC
         *      TEXT_PLAIN
         *      PERSISTENT_TEXT_PLAIN
         *          Delivery mode: 是否持久化，1 - Non-persistent，2 - Persistent
         *          Headers：Headers can have any name. Only long string headers can be set here.
         *          Properties: You can set other message properties here (delivery mode and headers are pulled out as the most common cases). Invalid properties will be ignored. Valid properties are:
         *          content_type ： 消息内容的类型
         *          content_encoding： 消息内容的编码格式
         *          priority： 消息的优先级
         *          correlation_id：关联id
         *          reply_to: 用于指定回复的队列的名称
         *          expiration： 消息的失效时间
         *          message_id： 消息id
         *          timestamp：消息的时间戳
         *          type： 类型
         *          user_id: 用户id
         *          app_id： 应用程序id
         *          cluster_id: 集群id
         *          Payload: 消息内容
         */
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY, MessageProperties.MINIMAL_PERSISTENT_BASIC,message.getBytes());
        channel.close();
        connection.close();
    }
}
