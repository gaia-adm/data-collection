package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.oncloud.GlobalSettings;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * Created by belozovs on 11/12/2015.
 */
public class MessageConsumer extends DefaultConsumer {

    private final int messagesLimit =  GlobalSettings.getWorkerPool();
    private volatile int messagesUnderProcessing;

    private Queue<ProviderConfig> localTasksQueue = new ArrayBlockingQueue<>(128);


    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public MessageConsumer(Channel channel) {
        super(channel);
        System.out.println("Message consumer created");
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

        messagesUnderProcessing++;
        System.out.println(" [*] Channel " + getChannel().toString() + Thread.currentThread().toString() + "Received: " + body.toString());
        System.out.println("Tag: " + consumerTag + ", deliveryTag: " + envelope.getDeliveryTag() + ", routing_key: " + envelope.getRoutingKey() + ", exchange: " + envelope.getExchange());
        System.out.println("Messages in progress*: " + messagesUnderProcessing);
        try {
            if(messagesUnderProcessing>messagesLimit-1){
                handleCancel(getConsumerTag());
                System.out.println("Stopped for " + getConsumerTag());
                Thread.sleep(30000);
                handleConsumeOk(getConsumerTag());
                System.out.println("Started for " + getConsumerTag());
            }

            System.out.println("Parsing next delivery...");
            //TODO - boris: prepare data, use temporary string meanwhile
            String bodyString = new String(body,"UTF-8");

            Map<String, String> propsMap = new HashMap<>();
            propsMap.put("deliveryTag", Long.toString(envelope.getDeliveryTag()));
            localTasksQueue.offer(new ProviderConfig("configId",envelope.getRoutingKey(), propsMap, "credentialsId", null, null));
            getChannel().basicAck(envelope.getDeliveryTag(), false);
            System.out.println("Task is stored in the local queue");
            messagesUnderProcessing--;
            System.out.println("Messages in progress**: " + messagesUnderProcessing);


/*            CompletableFuture.supplyAsync(() -> {
                        try {
                            System.out.println("Parsing next delivery...");
                            //TODO - boris: prepare data, use temporary string meanwhile
                            String bodyString = new String(body,"UTF-8");
                            localTasksQueue.offer(envelope.getRoutingKey()+" : "+envelope.getDeliveryTag() + " : " + bodyString);
                            getChannel().basicAck(envelope.getDeliveryTag(), false);
                            System.out.println("Task is stored in the local queue");
                            messagesUnderProcessing--;
                            System.out.println("Messages in progress**: " + messagesUnderProcessing);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

            );*/

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ProviderConfig getNextLocalTask() {
        return localTasksQueue.poll();
    }
}
