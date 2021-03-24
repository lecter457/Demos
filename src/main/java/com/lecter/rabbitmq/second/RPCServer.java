package com.lecter.rabbitmq.second;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RPCServer {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 5672;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Address[] addresses = new Address[]{
                new Address(IP_ADDRESS,PORT)
        };
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("root");
        factory.setPassword("root123");
        // 创建连接
        Connection connection = factory.newConnection(addresses);
        // 创建信道
        final Channel channel = connection.createChannel();
        // 设置客户端最多接受未被ack的消息的个数
        channel.basicQos(1);
        //
        channel.queueDeclare(RPC_QUEUE_NAME,false,false,false,null);

        Consumer consumer = new DefaultConsumer(channel){
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                AMQP.BasicProperties replyProps =new AMQP.BasicProperties().builder().correlationId(properties.getCorrelationId()).build();
                String response = "";
                try {
                    String message = new String(body,"UTF-8");
                    int n = Integer.parseInt(message);
                    System.out.println("[.]fib("+message+")");
                    response += fib(n);
                }catch (RuntimeException e){
                    System.out.println("[.]"+e.toString());
                }finally {
                    channel.basicPublish("",properties.getReplyTo(),replyProps,response.getBytes("UTF-8"));
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }

            }
        };
        channel.basicConsume(RPC_QUEUE_NAME,consumer);
        // 等待回调函数执行完毕之后，关闭资源
        TimeUnit.SECONDS.sleep(5);
        channel.close();
        connection.close();
    }

    private static int fib(int n) {
        if(n==0) return 0;
        if(n==1) return 0;
        return fib(n-1) + fib(n-2);
    }
}
