package com.lecter.rabbitmq.frist;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitConsumer {
    private static final String QUEUE_NAME = "queue_demo";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 5672;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Address[] addresses = new Address[]{new Address(IP_ADDRESS,PORT)};
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("root");
        factory.setPassword("root123");
        // 与节点建立连接
        Connection connection =factory.newConnection(addresses);
        // 连接创建信道
        final Channel channel = connection.createChannel();
        // 拉模式
        GetResponse response = channel.basicGet(QUEUE_NAME,false);// autoAck：是否自动确认
        System.out.println(new String(response.getBody()));
        channel.basicAck(response.getEnvelope().getDeliveryTag(),false);
        // 推模式
        // 设置客户端最多接收未被确认（ack）的消息个数
        channel.basicQos(64);
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                super.handleDelivery(consumerTag, envelope, properties, body);
                System.out.println("recv message:"+ new String(body));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channel.basicAck(envelope.getDeliveryTag(),false);
                /**
                 *   envelope.getDeliveryTag()：消息编码
                 *   requeue：是否重新进入队列
                  */
                channel.basicReject(envelope.getDeliveryTag(),true);
            }
        };
        /**
         * basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback)
         *      queue: 队列名称
         *      qutoAck: 是否自动确认
         *      consumerTag：消费者标签
         *      noLocal： 是否能将同一个connection中的生产者发送的消息传送给这个connection中的消费者，true为不能
         *      exclusive：是否排他
         *      arguments：其他参数
         *      callback：消费者的回调函数
         */
        channel.basicConsume(QUEUE_NAME,false,"1",consumer);
        TimeUnit.SECONDS.sleep(5);
        channel.close();
        connection.close();
    }

}
