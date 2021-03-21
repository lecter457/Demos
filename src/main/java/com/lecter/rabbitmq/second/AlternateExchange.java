package com.lecter.rabbitmq.second;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class AlternateExchange {
    // 交换器名称
    private static final String EXCHANCE_NAME="exchange_demo";
    // 路由键名称 用于绑定交换器
    private static final String ROUTING_KEY="routingkey_demo";
    // 绑定键名称 用于绑定队列
    private static final String BINDING_KEY="routingkey_demo";
    // 队列名称
    private static final String QUEUE_NAME="queue_demo";
    // ip地址
    private static final String IP_ADDRESS="127.0.0.1";
    // 端口号
    private static final int PORT = 5672;
    //    private static final Logger LOGGER = LoggerFxactory.getLogger(RabbitProducer.class);
    public static void main(String[] args) throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {

        ConnectionFactory factory =new ConnectionFactory();
        // 通过给定参数设置连接
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root123");
        // 创建连接
        Connection connection = factory.newConnection();

        // 创建信道
        Channel channel = connection.createChannel();
        /**
         * 备份交换器（alternate exchage）用途
         *      未被路由的消息会被放置在备份路由器当中
         *  =====================================
         *  备份交换器：
         *      1、当备份交换器不存在，客户端和rabbit服务端均不会抛出异常
         *      2、备份交换器没有绑定的队列，客户端和rabbit服务端均不会抛出异常
         *      3、备份交换器没有任何匹配的队列，客户端和rabbit服务端均不会抛出异常
         *      4、备份交换器和mandatory参数同时使用时，则mandatory参数无效
         */
        // 设置备份交换器参数
        Map<String,Object> aeArgs = new HashMap<String, Object>();
        aeArgs.put("alternate-exchages","myAe");
        // 创建备份交换器
        channel.exchangeDeclare("myAe","direct",true,false,null);
        // 创建正常交换器,并声明myAe交换器为exchange_demo交换器的备份交换器
        channel.exchangeDeclare(EXCHANCE_NAME,"direct",true,false,aeArgs);
        // 创建具有过期时间的队列
        /**
         * TTL(Time To Live):过期时间，消息在队列中存活时间超过过期时间，则会进入死信状态，消费者无法再次消费该消息，除非处理死信队列
         *          队列消息过期时间设置
         *              定时扫描队列内所有消息，过期自动删除
         *          *Policy方式设置过期时间
         *              rabbitmqctl set_policy TTL ".*" '{"message-ttl":60000}' --apply-to queues
         */
        // 设置过期时间队列参数
        Map<String,Object> ttlArgs = new HashMap<String, Object>();
        ttlArgs.put("x-message-ttl",6000);
        channel.queueDeclare(QUEUE_NAME,true,false,false,ttlArgs);
        // 路由和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANCE_NAME,ROUTING_KEY);
        // 路由和路由绑定
        channel.exchangeBind(EXCHANCE_NAME,EXCHANCE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello world";
        /**
         * 对每条消息设置过期时间
         *      待消息消费时，查看消息是否过期
         */
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.deliveryMode(2);//持久化消息
        builder.expiration("60000");// 设置TTL时间为60000
        AMQP.BasicProperties properties = builder.build();
        channel.basicPublish(EXCHANCE_NAME,BINDING_KEY, true,true,properties,message.getBytes());
        /**
         * mandatory设置为true，ReturnListenter用于监听返回到服务端的消息。
         *      当服务端没有找到对应队列时，Basic.Return返回消息，ReturnListenter监听返回事件
         */
        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
                String message = new String(body);
                System.out.println(message);
            }
        });
        channel.close();
        connection.close();
    }
}
