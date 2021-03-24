package com.lecter.rabbitmq.second;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class RPCClient {
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

    private Connection connection;

    private Channel channel;

    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    private QueueingConsumer consumer;

    public  RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory =new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root123");
        // 创建连接
        connection = factory.newConnection();
        // 创建信道
        channel =connection.createChannel();
        consumer =new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName,true,consumer);
//        channel.close();
//        connection.close();
    }

    public String call(String message) throws IOException, InterruptedException {
        String response  = null ;
        String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
        channel.basicPublish("",requestQueueName,props,message.getBytes());
        while (true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if(delivery.getProperties().getCorrelationId().equals(corrId)){
                response = new String(delivery.getBody());
                break;
            }
        }
        return response;
    }


    public void close() throws IOException {
        connection.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        RPCClient fibRPC = new RPCClient();
        System.out.println("[X] Requesting fib(30)");
        String respose = fibRPC.call("30");
        System.out.println("[.] Got '"+respose+"'");
        fibRPC.close();
    }
}

