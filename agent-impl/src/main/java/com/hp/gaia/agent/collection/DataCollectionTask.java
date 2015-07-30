package com.hp.gaia.agent.collection;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.Result;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.CredentialsService;
import com.hp.gaia.agent.service.DataProviderRegistry;
import com.hp.gaia.agent.service.ResultUploadService;
import com.hp.gaia.provider.CredentialsProvider;
import com.hp.gaia.provider.Data;
import com.hp.gaia.provider.DataProvider;
import com.hp.gaia.provider.DataStream;
import com.hp.gaia.provider.ProxyProvider;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Represents task that performs execution of data collection.
 */
public class DataCollectionTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(DataCollectionTask.class);

    private ProviderConfig providerConfig;

    private CollectionState collectionState;

    @Autowired
    private CollectionStateService collectionStateService;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private DataProviderRegistry dataProviderRegistry;

    @Autowired
    private ResultUploadService resultUploadService;

    // for tests
    public void setResultUploadService(final ResultUploadService resultUploadService) {
        this.resultUploadService = resultUploadService;
    }

    // for tests
    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void init(ProviderConfig providerConfig, CollectionState collectionState) {
        this.providerConfig = providerConfig;
        this.collectionState = collectionState;
    }

    @Override
    public void run() {
        collectionState.setState(State.RUNNING);
        collectionState.setLastCollectionTimestamp(System.currentTimeMillis());
        collectionStateService.saveCollectionState(collectionState);

        logger.debug("Executing data collection for configuration '" + providerConfig.getConfigId() +
                "' of data provider '" + providerConfig.getProviderId() + "'");

        try {
            fetchData();
            collectionState.setState(State.FINISHED);
            collectionState.setResult(Result.SUCCESS);
        } catch (Exception e) {
            logger.error("Execution of data collection for configuration '" + providerConfig.getConfigId() +
                    "' of data provider '" + providerConfig.getProviderId() + "' failed", e);
            collectionState.setState(State.FINISHED);
            collectionState.setResult(Result.FAILURE);
        } finally {
            // plan next execution (also in case of error)
            collectionState.setNextCollectionTimestamp(
                    System.currentTimeMillis() + providerConfig.getRunPeriod() * 60000L);
            collectionStateService.saveCollectionState(collectionState);
        }
    }

    /**
     * Performs execution of {@link DataProvider} and keeps updating bookmark in collection state.
     */
    private void fetchData() {
        DataProvider dataProvider = dataProviderRegistry.getDataProvider(providerConfig.getProviderId());
        CredentialsProvider credentialsProvider = new CredentialsProviderImpl(credentialsService,
                providerConfig.getCredentialsId());
        ProxyProvider proxyProvider = new ProxyProviderImpl(providerConfig.getProxy());

        DataStream dataStream = null;
        try {
            dataStream = dataProvider.fetchData(providerConfig.getProperties(), credentialsProvider,
                    proxyProvider, collectionState.getBookmark(), false);
            Data data = dataStream.next();
            while (data != null) {
                sendData(data);
                data = dataStream.next();
            }
        } finally {
            if (dataStream != null) {
                IOUtils.closeQuietly(dataStream);
            }
        }
    }

    private void sendData(final Data data) {
        resultUploadService.sendData(providerConfig, data);
        collectionState.setBookmark(data.bookmark());
        collectionStateService.saveCollectionState(collectionState);
        IOUtils.closeQuietly(data);
    }
}
