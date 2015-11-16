package com.hp.gaia.agent.oncloud.config;

import com.hp.gaia.agent.config.Credentials;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.service.CollectionState;

/**
 * Created by belozovs on 11/15/2015.
 *
 */
public class FullCollectionTask {

/*
   Example:
    {
    "tenantId": 1234567890,
    "providerConfig": {
        "configId": "1",
        "providerId": "alm/issue/change",
        "credentialsId": "qcCredentials",
        "properties": {
            "location": "http://mydtqc02.isr.hp.com:8080/qcbin",
            "project": "QC",
            "domain": "MERCURY",
            "init_history_days": 2
        }
    },
    "credentials": {
        "credentialsId": "qcCredentials",
        "values": {
            "password": {
                "plain": "bbb"
            },
            "username": {
                "plain": "aaa"
            }
        }
    },
    "collectionState": {
        "state": "PENDING"
    }
}
*/

    private long tenantId;
    private ProviderConfig providerConfig;
    private Credentials credentials;
    private CollectionState collectionState;

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public CollectionState getCollectionState() {
        collectionState.setProviderConfigId(providerConfig.getConfigId());
        return collectionState;
    }

    public void setCollectionState(CollectionState collectionState) {
        this.collectionState = collectionState;
    }
}
