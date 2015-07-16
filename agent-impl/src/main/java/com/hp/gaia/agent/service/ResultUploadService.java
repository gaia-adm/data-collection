package com.hp.gaia.agent.service;

import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.provider.Data;

/**
 * Client service for uploading data to GAIA result upload service.
 */
public interface ResultUploadService {

    void sendData(ProviderConfig providerConfig, Data data);
}
