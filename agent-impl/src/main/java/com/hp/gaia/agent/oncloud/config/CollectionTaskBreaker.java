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

    private final static String concatConnector = ":";

    @Autowired
    UpdatableCredentialsService credentialsService;

    @Autowired
    CollectionStateService collectionStateService;

    //Todo - boris: there is no cleanup for maps, good for beginning but not for production. Think about adding ttl and cleanByTtl on each setter or something like this

    //credentials is in use only when running fetchData, it is always taken from credentialsMap; we no need to play with its ID in other places
    public void setCredentials(long tenantId, Credentials credentials) {
        //Todo - boris: Encrypted/Plain? Currently nothing assumed as encrypted
        credentialsService.addCredentials(concat(String.valueOf(tenantId), credentials.getCredentialsId()), credentials);

    }

    //collection state is in use multiple times from multiple places, so its ID is set at the beginning to fit provider's configId
    public void setCollectionState(long tenantId, CollectionState collectionState) {
        collectionStateService.saveCollectionState(collectionState);
    }

    public static String concat(String str1, String str2){
        return str1.concat(concatConnector).concat(str2);
    }
}
