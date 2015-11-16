package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.oncloud.GlobalSettings;
import com.hp.gaia.agent.oncloud.config.CollectionTaskBreaker;
import com.hp.gaia.agent.oncloud.config.FullCollectionTask;
import com.hp.gaia.agent.service.DataProviderRegistry;
import com.hp.gaia.agent.service.ProvidersConfigService;
import com.hp.gaia.provider.DataProvider;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class OnCloudProvidersConfigService implements ProvidersConfigService {

    private final static String EXCHANGE_NAME = "tasksTopic";
    private final static int NEXT_DELIVERY_TIMEOUT = 5000;  //5 sec

    private Connection connection = null;
    private Channel channel = null;
    private DefaultConsumer consumer = null;

    @Autowired
    private DataProviderRegistry dataProviderRegistry;


    private static final int DEFAULT_RUN_PERIOD = 60; // every 60 minutes

    private Map<String, ProviderConfig> providerConfigMap;

    @Autowired
    CollectionTaskBreaker collectionTaskBreaker;

/*
    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;
*/

    public void init() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(GlobalSettings.getRabbitMqHost());
        factory.setPort(GlobalSettings.getRabbitMqPort());
        factory.setUsername(GlobalSettings.getRabbitMqUser());
        factory.setPassword(GlobalSettings.getRabbitMqPassword());
        factory.setAutomaticRecoveryEnabled(true); // connection that will recover automatically
        factory.setNetworkRecoveryInterval(10000); // attempt recovery to the max of 10 seconds
        factory.setRequestedHeartbeat(30); // Setting heartbeat to 30 sec instead the default of 10 min
        // that way the client will know that server is unreachable
        // and will try to reconnect
        // The same is true for the server (the broker) - after 30 sec
        // he will close the consumers.

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
            channel.basicQos(1);

            int queuesCount = 0;
            for (DataProvider dataProvider : dataProviderRegistry.getDataProviders()) {
                //create queue, if does not exist
                channel.queueDeclare("task/" + dataProvider.getProviderId(), true, false, false, null);
                channel.queueBind("task/" + dataProvider.getProviderId(), EXCHANGE_NAME, dataProvider.getProviderId());
                queuesCount++;
            }
            System.out.println("Consuming from " + queuesCount + " queues");

            consumer = new MessageConsumer(channel);
            for (DataProvider dataProvider : dataProviderRegistry.getDataProviders()) {
                channel.basicConsume("task/" + dataProvider.getProviderId(), false, consumer);
                System.out.println("Consumption started for  from task/" + dataProvider.getProviderId());
            }

            consumer.handleCancel(consumer.getConsumerTag());
            System.out.println("Done with RabbitMQ preparations and channel paused");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

/*        ProvidersConfig providersConfig = ConfigUtils.readConfig(providersConfigFile, ProvidersConfig.class);

        this.providerConfigMap = new HashMap<>();
        final Proxy globalProxy = providersConfig.getProxy();

        final List<ProviderConfig> providers = providersConfig.getProviders();
        validate(providers);*/
/*        boolean saveNewFile = encryptNeededValues(providersConfig);
        if (saveNewFile) {
            File newConfigFile = new File(providersConfigFile.getAbsolutePath() + ".encrypted");
            if (!newConfigFile.exists() || newConfigFile.canWrite()) {
                ConfigUtils.writeConfig(newConfigFile, providersConfig);
            }
        }
        decryptValues(providersConfig);*/
        // store provider configs in local map
/*        if (providers != null) {
            for (final ProviderConfig providerConfig : providers) {
                providerConfigMap.put(providerConfig.getConfigId(),
                        makeSafeProviderConfig(providerConfig, globalProxy));
            }
        }*/
    }

    /**
     * Creates safe {@link ProviderConfig} that can be passed outside of the service.
     */
/*    private static ProviderConfig makeSafeProviderConfig(final ProviderConfig providerConfig, final Proxy globalProxy) {
        return new ProviderConfig(providerConfig.getConfigId(), providerConfig.getProviderId(),
                providerConfig.getProperties() != null ?
                        Collections.unmodifiableMap(providerConfig.getProperties()) :
                        Collections.emptyMap(), providerConfig.getCredentialsId(),
                providerConfig.getProxy() == null ? globalProxy : providerConfig.getProxy(),
                providerConfig.getRunPeriod() == null ? DEFAULT_RUN_PERIOD : providerConfig.getRunPeriod());
    }*/
    @Override
    public ProviderConfig getProviderConfig(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        final ProviderConfig providerConfig = providerConfigMap.get(providerConfigId);
        if (providerConfig == null) {
            throw new IllegalArgumentException("Invalid providerConfigId " + providerConfigId);
        }

        return providerConfig;
    }

    public List<ProviderConfig> getProviderConfigs() {

        List<ProviderConfig> result = new ArrayList<>();

        consumer.handleConsumeOk(consumer.getConsumerTag());
        FullCollectionTask fct = ((MessageConsumer) consumer).getNextLocalTask();

        if (fct != null && fct.getProviderConfig() != null) {
            long tenantId = fct.getTenantId();
            ProviderConfig pc = fct.getProviderConfig();
            pc.setConfigId(CollectionTaskBreaker.concat(String.valueOf(tenantId),pc.getConfigId()));
            pc.setCredentialsId(CollectionTaskBreaker.concat(String.valueOf(tenantId),pc.getCredentialsId()));
            collectionTaskBreaker.setCredentials(tenantId, fct.getCredentials());
            collectionTaskBreaker.setCollectionState(tenantId, fct.getCollectionState());

            result.add(pc);
        }

        return result;

/*        QueueingConsumer.Delivery delivery = null;
        System.out.println("Checking for the next delivery...");
        try {
            delivery = consumer.nextDelivery(NEXT_DELIVERY_TIMEOUT);
            if (delivery != null) {
                String message = new String(delivery.getBody());
                String routingKey = delivery.getEnvelope().getRoutingKey();
                System.out.println(" [x] Received '" + message + "' with routing key " + routingKey);
                result.add(new ProviderConfig("configId", "providerId", new HashMap<String, String>(), "credentialsId", null, 0));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot ack, sorry...");
            e.printStackTrace();
        }*/


    }

    @Override
    public boolean isProviderConfig(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        return providerConfigMap.containsKey(providerConfigId);
    }

/*    private static void validate(final List<ProviderConfig> providers) {
        Set<String> providerConfigIds = new HashSet<>();
        if (providers != null) {
            for (final ProviderConfig providerConfig : providers) {
                validate(providerConfig);
            }
            for (final ProviderConfig providerConfig : providers) {
                if (providerConfigIds.contains(providerConfig.getConfigId())) {
                    throw new IllegalStateException("Duplicate provider configurationId " + providerConfig.getConfigId());
                }
                providerConfigIds.add(providerConfig.getConfigId());
            }
        }
    }*/

/*    private static void validate(final ProviderConfig providerConfig) {
        Validate.notNull(providerConfig);
        if (StringUtils.isEmpty(providerConfig.getConfigId())) {
            throw new IllegalStateException("configId cannot be null or empty");
        }
        if (StringUtils.isEmpty(providerConfig.getProviderId())) {
            throw new IllegalStateException("providerId cannot be null or empty");
        }
        if (providerConfig.getRunPeriod() != null && providerConfig.getRunPeriod() <= 0) {
            throw new IllegalStateException("runPeriod cannot be negative");
        }
        if (providerConfig.getProxy() != null && !StringUtils.isEmpty(providerConfig.getProxy().getHttpProxy())) {
            // validate proxy URL
            providerConfig.getProxy().getHttpProxyURL();
        }
    }*/

/*    private void decryptValues(final ProvidersConfig providersConfig) {
        decryptProxyPassword(providersConfig.getProxy());
        final List<ProviderConfig> providerConfigs = providersConfig.getProviders();
        if (providerConfigs != null) {
            for (ProviderConfig providerConfig : providerConfigs) {
                decryptProxyPassword(providerConfig.getProxy());
            }
        }
    }*/

/*    private void decryptProxyPassword(Proxy proxy) {
        if (proxy != null) {
            if (proxy.getHttpProxyPassword() != null && proxy.getHttpProxyPassword().getType() == Type.ENCRYPTED) {
                String newProxyPassword = protectedValueDecrypter.decrypt(proxy.getHttpProxyPassword());
                proxy.setHttpProxyPassword(new ProtectedValue(Type.PLAIN, newProxyPassword));
            }
        }
    }*/

/*    private boolean encryptNeededValues(final ProvidersConfig providersConfig) {
        boolean result = encryptProxyPassword(providersConfig.getProxy());
        final List<ProviderConfig> providerConfigs = providersConfig.getProviders();
        if (providerConfigs != null) {
            for (ProviderConfig providerConfig : providerConfigs) {
                if (encryptProxyPassword(providerConfig.getProxy())) {
                    result = true;
                }
            }
        }

        return result;
    }*/

/*    private boolean encryptProxyPassword(Proxy proxy) {
        if (proxy != null) {
            if (proxy.getHttpProxyPassword() != null && proxy.getHttpProxyPassword().getType() == Type.ENCRYPT) {
                ProtectedValue newProxyPassword = protectedValueDecrypter.encrypt(proxy.getHttpProxyPassword().getValue());
                proxy.setHttpProxyPassword(newProxyPassword);
                return true;
            }
        }
        return false;
    }*/
}
