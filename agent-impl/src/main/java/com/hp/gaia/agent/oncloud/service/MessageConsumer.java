package com.hp.gaia.agent.oncloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.gaia.agent.oncloud.GlobalSettings;
import com.hp.gaia.agent.oncloud.config.FullCollectionTask;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is actually a singleton because constructed in {@link OnCloudProvidersConfigService} init method called only once
 */
public class MessageConsumer extends DefaultConsumer {

    private final int messagesLimit = GlobalSettings.getWorkerPool();
    private final AtomicInteger messagesUnderProcessing = new AtomicInteger();

    private Queue<FullCollectionTask> localTasksQueue = new ArrayBlockingQueue<>(128);


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


        System.out.println(" [*] Channel " + getChannel().toString() + Thread.currentThread().toString() + "Received: " + new String(body));
        System.out.println("Tag: " + consumerTag + ", deliveryTag: " + envelope.getDeliveryTag() + ", routing_key: " + envelope.getRoutingKey() + ", exchange: " + envelope.getExchange());
        System.out.println("Messages in progress before handling delivery*: " + messagesUnderProcessing.get());
        try {
            if (messagesUnderProcessing.get() > messagesLimit - 1) {
                handleCancel(getConsumerTag());
                System.out.println("Stopped for " + getConsumerTag());
                //TODO - boris: make it in separate thread (see supplyAsync below) and in loop to wait for queue size decrease and not just for 30 seconds
                Thread.sleep(30000);
                handleConsumeOk(getConsumerTag());
                System.out.println("Started for " + getConsumerTag());
            }

            System.out.println("Parsing next delivery...");
            if (localTasksQueue.offer(new ObjectMapper().readValue(new String(body), FullCollectionTask.class))) {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                System.out.println("Task is stored in the local queue");
                System.out.println("Messages in progress**: " + messagesUnderProcessing.incrementAndGet());
            } else {
                getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                System.out.println("Task is NOT stored in the local queue, returned to RabbitMQ");
            }


/*            CompletableFuture.supplyAsync(() -> {
                        try {
                            System.out.println("Parsing next delivery...");
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

    public FullCollectionTask getNextLocalTask() {
        FullCollectionTask fct = localTasksQueue.poll();
        if (fct != null) {
            messagesUnderProcessing.getAndDecrement();
        }
        return fct;
    }
}
