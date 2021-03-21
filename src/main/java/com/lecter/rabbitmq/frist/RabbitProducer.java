package com.lecter.rabbitmq.frist;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitProducer {
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
//        factory.setHost(IP_ADDRESS);
//        factory.setPort(PORT);
//        factory.setUsername("root");
//        factory.setPassword("root123");
        /**
         * 通过URL创建连接
         */
        factory.setUri("amqp://username:password@ipAddress:protNumber/virtualHost");
        // 创建连接
        Connection connection = factory.newConnection();
        if(connection.isOpen()){
            System.out.println("连接已建立");
        }

        // 创建信道
        Channel channel = connection.createChannel();

        if(channel.isOpen()){
            System.out.println("信道已建立");
        }
        // 创建type="direct"，持久化的，非自动删除的交换器
        /**
         *  创建type="direct"，持久化的，非自动删除的交换器
         *   参数：1.交换器名称，
         *        2.交换器类型，
         *              fanout: 广播类型，将消息路由到所有与该交换器绑定的队列中
         *              direct： 点对点类型，将消息路由到绑定键和路由键完全一致的队列中
         *              topic：主题类型，类似direct，但是可以设置匹配规则，路由键和绑定键为一个点号分隔的字符串。绑定键可以存在两种特殊字符*和#。*用于匹配单个单词，#用于匹配多个单词也可以是0个
         *              headers： 不依赖路由键的匹配规则来路由消息，而是根据发送消息内容中的headers属性进行匹配。性能很差，不推荐使用
         *        3.是否持久化，
         *              持久化可以将交换器存盘
         *        4.是否自动删除，
         *              当与此交换器连接的客户端全部断开，则自动删除此交换器。前提是已经有过连接了
         *        5.是否是内置交换器，
         *              表示该交换器为内置交换器，只能通过交换器路由到交换器
         *        6.结构化参数
          */
        channel.exchangeDeclare(EXCHANCE_NAME,"direct",true,false,null);
        /**
         *  创建一个持久化，非排他的，非自动删除的队列
         *  参数：1、队列名称
         *       2、设置是否持久化
         *          持久化队列可以存盘，保证消息不丢失
         *       3、是否排他
         *          声明为排他队列，则该队列仅对初次连接可见，并在连接断开后或者客户端退出自动删除
         *       4、是否自动删除
         *          当与此队列连接的客户端全部断开，则自动删除此队列。前提是已经有过连接了
         *       5、队列的其他参数
          */

        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        // 创建匿名队列。由rabbit命名，排他、自动删除、非持久化的队列
        AMQP.Queue.DeclareOk ok = channel.queueDeclare();
        ok.getQueue();
        // 路由和队列绑定
        channel.queueBind(QUEUE_NAME,EXCHANCE_NAME,ROUTING_KEY);
        // 路由和路由绑定
        channel.exchangeBind(EXCHANCE_NAME,EXCHANCE_NAME,ROUTING_KEY);
        // 发送一条持久化的消息
        String message = "Hello world";
        byte[] messageBodyBytes = message.getBytes();
        /**
         * MessageProperties
         *          delivery mode（投递模式） : 2.消息会持久化
         *          priority(优先级): 1
         * basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props, byte[] body)
         *         exchange：交换器名称
         *         routingKey：路由键
         *         mandatory：没有找到队列时，true则返回给生产者，flase则直接丢弃
         *         immediate：队列是否存在消费者：true则返回则生产者，false则直接丢弃
         *         props：投递参数
         *         body：消息体
         */
        channel.basicPublish(EXCHANCE_NAME,BINDING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        channel.close();
        connection.close();
    }

}
