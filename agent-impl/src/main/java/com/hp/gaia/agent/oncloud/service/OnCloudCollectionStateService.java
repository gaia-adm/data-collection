package com.hp.gaia.agent.oncloud.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.Result;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.ProvidersConfigService;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnCloudCollectionStateService implements CollectionStateService {

    private static final Logger logger = LogManager.getLogger(OnCloudCollectionStateService.class);

    private Map<String, CollectionState> collectionStateMap;

    private ObjectMapper objectMapper;

    private File stateDir;

    @Autowired
    private ProvidersConfigService providersConfigService;

    public OnCloudCollectionStateService() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    public void init() {
        System.out.println("OnCloudCollectionStateService initialized");
    }

    private List<CollectionState> readAllCollectionStates(File stateDir) {
        List<CollectionState> resultList = new ArrayList<>();
        File[] files = stateDir.listFiles(new StateFileFilter());

        for (File file : files) {
            try {
                CollectionState collectionState = objectMapper.readValue(file, CollectionState.class);

                if (providersConfigService.isProviderConfig(collectionState.getProviderConfigId())) {
                    fixCollectionState(collectionState);
                    resultList.add(collectionState);
                } else {
                    logger.warn("Found obsolete collection state " + file.getName() +
                            " without valid provider configuration. Ignoring...");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse collection state file " + file.getName(), e);
            }
        }

        return resultList;
    }

    /**
     * Fixes an existing {@link CollectionState}. For example it may have state marked as RUNNING or PENDING, but
     * since we are starting it may not be true.
     */
    private void fixCollectionState(final CollectionState collectionState) {
        if (collectionState.getState() == State.PENDING || collectionState.getState() == State.RUNNING) {
            collectionState.setState(State.FINISHED);
            collectionState.setResult(Result.FAILURE);
            saveToFile(collectionState);
        }
    }

    @Override
    public CollectionState getCollectionState(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        if (!providersConfigService.isProviderConfig(providerConfigId)) {
            throw new IllegalArgumentException(providerConfigId + " is not a valid provider configuration id");
        }
        return collectionStateMap.get(providerConfigId);
    }

    @Override
    public void saveCollectionState(final CollectionState collectionState) {
        Validate.notNull(collectionState);

        String providerConfigId = collectionState.getProviderConfigId();
        if (!providersConfigService.isProviderConfig(providerConfigId)) {
            throw new IllegalArgumentException(providerConfigId + " is not a valid provider configuration id");
        }
        // save the state into file
        saveToFile(collectionState);

        // save locally
        collectionStateMap.put(providerConfigId, (CollectionState) ObjectUtils.clone(collectionState));
    }

    private void saveToFile(final CollectionState collectionState) {
        try {
            objectMapper.writeValue(getStateFile(collectionState.getProviderConfigId()), collectionState);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save collection state", e);
        }
    }

    void deleteCollectionState(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        if (!getStateFile(providerConfigId).delete()) {
            throw new RuntimeException("Failed to delete state for configuration id " + providerConfigId);
        }
        collectionStateMap.remove(providerConfigId);
    }

    private File getStateFile(final String providerConfigId) {
        return new File(stateDir, providerConfigId + "-state.json");
    }

    private static class StateFileFilter implements FilenameFilter {

        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".json");
        }
    }
}
