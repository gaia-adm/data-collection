package com.hp.gaia.agent.service;

/**
 * Keeps state of data collection via data providers. Allows to store state like last bookmark, last data collection
 * date etc.
 */
public interface CollectionStateService {

    /**
     * Gets state of data collection for given configuration id.
     *
     * @return null if no {@link CollectionState} was found for given valid providerConfigId
     * @throws IllegalArgumentException if providerConfigId is invalid
     */
    CollectionState getCollectionState(String providerConfigId);

    /**
     * Saves state of data collection for given configuration id.
     */
    void saveCollectionState(CollectionState collectionState);
}
