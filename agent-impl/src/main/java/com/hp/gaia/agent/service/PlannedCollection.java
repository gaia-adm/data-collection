package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.ProviderConfig;

/**
 * Represents data collection that should be executed.
 */
public class PlannedCollection {

    private ProviderConfig providerConfig;

    private CollectionState collectionState;

    public PlannedCollection(final ProviderConfig providerConfig, final CollectionState collectionState) {
        this.providerConfig = providerConfig;
        this.collectionState = collectionState;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public CollectionState getCollectionState() {
        return collectionState;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlannedCollection{");
        sb.append("providerConfig=").append(providerConfig);
        sb.append(", collectionState=").append(collectionState);
        sb.append('}');
        return sb.toString();
    }
}
