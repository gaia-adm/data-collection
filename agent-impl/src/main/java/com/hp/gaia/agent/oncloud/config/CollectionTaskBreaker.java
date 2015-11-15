package com.hp.gaia.agent.oncloud.config;

import com.hp.gaia.agent.config.Credentials;
import com.hp.gaia.agent.service.CollectionState;
import com.hp.gaia.agent.service.CollectionStateService;
import com.hp.gaia.agent.service.UpdatableCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by belozovs on 11/15/2015.
 * <p>
 * Populate "auxiliary" service maps (credentials, state) with data arrived inside {@link FullCollectionTask}.
 * After this step the flow switch from using "transport-only" {@link FullCollectionTask} to the "standard" way used by onPrem agent too
 */
@Component
public class CollectionTaskBreaker {

    @Autowired
    UpdatableCredentialsService credentialsService;

    @Autowired
    CollectionStateService collectionStateService;

    //Todo - boris: there is no cleanup for maps, good for beginning but not for production. Think about adding ttl and cleanByTtl on each setter or something like this

    public void setCredentials(long tenantId, Credentials credentials) {
        //Todo - boris: Encrypted/Plain? Currently nothing assumed as encrypted
        credentialsService.addCredentials(tenantId + credentials.getCredentialsId(), credentials);

    }

    public void setCollectionState(long tenantId, CollectionState collectionState) {
        collectionState.setProviderConfigId(tenantId + collectionState.getProviderConfigId());
        collectionStateService.saveCollectionState(collectionState);
    }
}
