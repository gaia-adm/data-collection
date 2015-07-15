package com.hp.gaia.agent.onprem.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.gaia.agent.onprem.GlobalSettings;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.ProvidersConfigService;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnPremCollectionStateService implements CollectionStateService {

    private static final Logger logger = LogManager.getLogger(OnPremCollectionStateService.class);

    private static final String STATE_DIR_NAME = "state";

    private Map<String, CollectionState> collectionStateMap;

    private ObjectMapper objectMapper;

    @Autowired
    private ProvidersConfigService providersConfigService;

    public OnPremCollectionStateService() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    @PostConstruct
    public void init() {
        // do basic validation
        File stateDir = getStateDir();
        if (!stateDir.exists()) {
            throw new IllegalStateException("Directory " + stateDir.getAbsolutePath() + " doesn't exist");
        }
        if (!stateDir.isDirectory()) {
            throw new IllegalStateException(stateDir.getAbsolutePath() + " is not a directory");
        }
        collectionStateMap = new HashMap<>();
        // read all state files
        List<CollectionState> collectionStates = readAllCollectionStates(stateDir);
        for (CollectionState collectionState : collectionStates) {
            collectionStateMap.put(collectionState.getProviderConfigId(), collectionState);
        }
    }

    private List<CollectionState> readAllCollectionStates(File stateDir) {
        List<CollectionState> resultList = new ArrayList<>();
        File[] files = stateDir.listFiles(new StateFileFilter());

        for (File file : files) {
            try {
                CollectionState collectionState = objectMapper.readValue(file, CollectionState.class);

                if (providersConfigService.isProviderConfig(collectionState.getProviderConfigId())) {
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
        try {
            objectMapper.writeValue(getStateFile(collectionState.getProviderConfigId()), collectionState);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save collection state", e);
        }
        // save locally
        collectionStateMap.put(providerConfigId, collectionState);
    }

    void deleteCollectionState(final String providerConfigId) {
        Validate.notNull(providerConfigId);

        if (!getStateFile(providerConfigId).delete()) {
            throw new RuntimeException("Failed to delete state for configuration id " + providerConfigId);
        }
        collectionStateMap.remove(providerConfigId);
    }

    private static File getStateFile(final String providerConfigId) {
        return new File(getStateDir(), providerConfigId + "-state.json");
    }

    private static File getStateDir() {
        return new File(GlobalSettings.getWorkingDir(), STATE_DIR_NAME);
    }

    private static class StateFileFilter implements FilenameFilter {

        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".json");
        }
    }
}
