package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.service.PlannedCollectionProvider;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionState.State;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.PlannedCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OnPremPlannedCollectionProvider implements PlannedCollectionProvider {

    @Autowired
    private CollectionStateService collectionStateService;

    @Autowired
    private OnPremProvidersConfigService onPremProvidersConfigService;

    @Override
    public PlannedCollection findNextPlannedCollection() {
        List<ProviderConfig> configList = onPremProvidersConfigService.getProviderConfigs();
        PlannedCollection plannedCollection = null;
        long currentTimestamp = System.currentTimeMillis();
        for (ProviderConfig providerConfig : configList) {
            CollectionState collectionState = collectionStateService.getCollectionState(providerConfig.getConfigId());
            if (collectionState == null) {
                // 1st priority is configurations that never had data collections
                collectionState = new CollectionState(providerConfig.getConfigId());
                return new PlannedCollection(providerConfig, collectionState);
            }
            if (collectionState.getState() != State.RUNNING && collectionState.getState() != State.PENDING) {
                if (plannedCollection == null) {
                    // select first not running configuration
                    if (collectionState.getNextCollectionTimestamp() == null ||
                            collectionState.getNextCollectionTimestamp() <= currentTimestamp) {
                        plannedCollection = new PlannedCollection(providerConfig, collectionState);
                    }
                } else if (isPreferred(collectionState, plannedCollection.getCollectionState(), currentTimestamp)) {
                    plannedCollection = new PlannedCollection(providerConfig, collectionState);
                }
            }
        }
        return plannedCollection;
    }

    /**
     * Returns true if collectionState1 should be preferred over collectionState2.
     */
    private static boolean isPreferred(CollectionState collectionState1, CollectionState collectionState2, long currentTimestamp) {
        if (collectionState2.getLastCollectionTimestamp() == null ||
                collectionState2.getNextCollectionTimestamp() == null) {
            return false;
        }
        if (collectionState1.getLastCollectionTimestamp() == null ||
                collectionState1.getNextCollectionTimestamp() == null) {
            return true;
        }
        if (collectionState1.getNextCollectionTimestamp() <= currentTimestamp) {
            if (collectionState2.getNextCollectionTimestamp() < collectionState1.getNextCollectionTimestamp()) {
                return false;
            } else if (collectionState2.getNextCollectionTimestamp() > collectionState1.getNextCollectionTimestamp()) {
                return true;
            } else {
                return collectionState2.getLastCollectionTimestamp() - collectionState1.getLastCollectionTimestamp() > 0L;
            }
        } else {
            return false;
        }
    }
}
