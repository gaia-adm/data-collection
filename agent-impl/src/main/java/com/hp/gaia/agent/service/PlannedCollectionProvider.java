package com.hp.gaia.agent.service;

/**
 * Performs selection of data provider configuration for data collection.
 */
public interface PlannedCollectionProvider {

    /**
     * Finds candidate for the next data collection. May return <code>null</code> if there is no data collection planned
     * to be executed now.
     */
    PlannedCollection findNextPlannedCollection();
}
